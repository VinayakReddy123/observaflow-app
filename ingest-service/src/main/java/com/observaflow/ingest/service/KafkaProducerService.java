package com.observaflow.ingest.service;

import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;

import com.observaflow.ingest.model.TelemetryEvent;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final ReactiveKafkaProducerTemplate<String, TelemetryEvent> kafkaTemplate;
    private final MeterRegistry meterRegistry;
    private static final String TOPIC = "raw-telemetry";

    public Mono<Void> publish(TelemetryEvent event) {
        return kafkaTemplate.send(TOPIC, event.getTenantId(), event)
                .doOnSuccess(result -> {
                    var metadata = result.recordMetadata();
                    log.info(
                            "Published telemetry event | tenant={} type={} topic={} partition={} offset={}",
                            event.getTenantId(),
                            event.getType(),
                            metadata.topic(),
                            metadata.partition(),
                            metadata.offset());
                    meterRegistry.counter("ingest_events_total", "type", event.getType().name()).increment();
                })
                .doOnError(error -> log.error(
                        "Failed to publish telemetry event | tenant={} type={} error={}",
                        event.getTenantId(),
                        event.getType(),
                        error.getMessage()))
                .then(); // .then() converts Mono<SenderResult> to Mono<Void>
    }

}
