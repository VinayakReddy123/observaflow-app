package com.observaflow.queryservice.consumer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Component;

import com.observaflow.queryservice.model.TelemetryEvent;
import com.observaflow.queryservice.repository.TelemetryEventRepository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class EventStorageConsumer {

    private final ReactiveKafkaConsumerTemplate<String, TelemetryEvent> consumerTemplate;
    private final TelemetryEventRepository repository;

    public EventStorageConsumer(
            @Qualifier("eventsConsumerTemplate") ReactiveKafkaConsumerTemplate<String, TelemetryEvent> consumerTemplate,
            TelemetryEventRepository repository) {
        this.consumerTemplate = consumerTemplate;
        this.repository = repository;
    }

    @PostConstruct
    public void startConsuming() {
        consumerTemplate.receiveAutoAck()
                .filter(record -> record.value().getType() != TelemetryEvent.EventType.METRIC)
                .flatMap(record -> repository.save(record.value())
                        .doOnSuccess(saved -> log.info("Stored Events | tenantId={} serviceId={}",
                                saved.getTenantId(), saved.getServiceId()))
                        .onErrorResume(e -> {
                            log.error("Failed to store event | reason={}", e.getMessage());
                            return Mono.empty();
                        }))
                .subscribe(
                        v -> {
                        },
                        error -> log.error("Events consumer stream terminated | reason={}", error.getMessage()));
    }
}
