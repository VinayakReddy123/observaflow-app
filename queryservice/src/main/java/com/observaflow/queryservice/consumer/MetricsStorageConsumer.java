package com.observaflow.queryservice.consumer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Component;

import com.observaflow.queryservice.model.ProcessedMetric;
import com.observaflow.queryservice.repository.ProcessedMetricRepository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class MetricsStorageConsumer {
    
    private final ReactiveKafkaConsumerTemplate<String,ProcessedMetric> consumerTemplate;
    private final ProcessedMetricRepository repository;

    public MetricsStorageConsumer(
        @Qualifier("metricsConsumerTemplate") ReactiveKafkaConsumerTemplate<String,ProcessedMetric> consumerTemplate ,
        ProcessedMetricRepository repository
    ){
        this.consumerTemplate = consumerTemplate;
        this.repository = repository;
    }

    @PostConstruct
    public void startConsuming(){
        consumerTemplate.receiveAutoAck()
             .flatMap(record -> repository.save(record.value())
                     .doOnSuccess(saved -> log.info("Stored metric | tenantId={} serviceId={}",
                                saved.getTenantId(), saved.getServiceId()))
                     .onErrorResume(e -> {
                            log.error("Failed to store metric | reason={}", e.getMessage());
                            return Mono.empty();
                    }))
             .subscribe(
                        v -> {},
                        error -> log.error("Metrics consumer stream terminated | reason={}", error.getMessage())
              );
    }
}
