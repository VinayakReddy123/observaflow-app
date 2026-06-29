package com.observaflow.alert.consumer;

import com.observaflow.alert.model.AlertEvent;
import com.observaflow.alert.model.ProcessedMetric;
import com.observaflow.alert.service.AlertEvaluatorService;
import com.observaflow.alert.service.WebhookService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class MetricsConsumer {


    private final ReactiveKafkaConsumerTemplate<String, ProcessedMetric> consumerTemplate;
    private final AlertEvaluatorService evaluatorService;
    private final ReactiveKafkaProducerTemplate<String, AlertEvent> alertProducerTemplate;
    private final WebhookService webhookService;

    @PostConstruct
    public void startConsuming() {
        consumerTemplate.receiveAutoAck()
                .flatMap(record -> processMetric(record.value())
                        .onErrorResume(e -> {
                            log.error("Failed to process metric for key={} | reason: {}",
                                    record.key(), e.getMessage());
                            return Mono.empty();
                        }))
                .subscribe(
                        v -> {},
                        error -> log.error("Kafka consumer stream terminated unexpectedly | reason: {}",
                                error.getMessage())
                );
    }

    private Mono<Void> processMetric(ProcessedMetric metric) {
        return evaluatorService.evaluate(metric)
                .flatMap(event ->
                        alertProducerTemplate.send("alert-events", event.getTenantId(), event)
                                .doOnSuccess(result -> log.info(
                                        "Alert fired | tenantId={} | alertId={} | topic=alert-events",
                                        event.getTenantId(), event.getRuleId()))
                                .then(webhookService.fire(event.getWebhookUrl(), event))
                )
                .then();
    }
}