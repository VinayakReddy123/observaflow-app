package com.observaflow.websocket.consumer;

import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.observaflow.websocket.model.ProcessedMetric;
import com.observaflow.websocket.registry.SessionRegistry;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsConsumer {

    private final ReactiveKafkaConsumerTemplate<String, ProcessedMetric> consumerTemplate;
    private final SessionRegistry registry;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void startConsuming() {
        consumerTemplate.receiveAutoAck()
                .flatMap(record -> fanOut(record.value())
                        .onErrorResume(e -> {
                            log.error("Fan-out failed | tenantId={} reason={}",
                                    record.value().getTenantId(), e.getMessage());
                            return Mono.empty();
                        }))
                .subscribe(
                        v -> {
                        },
                        error -> log.error("Consumer stream terminated | reason={}", error.getMessage()));
    }

    private Mono<Void> fanOut(ProcessedMetric metric) {
        var sessions = registry.getSessions(metric.getTenantId());

        if (sessions.isEmpty()) {
            return Mono.empty();
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(metric);
        } catch (JsonProcessingException e) {
            log.error("Serialization failed | reason={}", e.getMessage());
            return Mono.empty();
        }

        return Flux.fromIterable(sessions)
                .flatMap(session -> session.send(Mono.just(session.textMessage(json)))
                        .onErrorResume(e -> {
                            log.warn("Push failed | sessionId={} reason={}", session.getId(), e.getMessage());
                            registry.remove(metric.getTenantId(), session);
                            return Mono.empty();
                        }))
                .then();
    }
}
