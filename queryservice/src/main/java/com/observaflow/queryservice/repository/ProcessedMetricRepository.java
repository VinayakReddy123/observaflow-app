package com.observaflow.queryservice.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.observaflow.queryservice.model.ProcessedMetric;

import reactor.core.publisher.Flux;

public interface ProcessedMetricRepository extends ReactiveMongoRepository<ProcessedMetric,String> {
    
    Flux<ProcessedMetric> findByTenantIdAndServiceIdAndWindowStartBetween(
        String tenantId, String serviceId, long from, long to
    );

    Flux<ProcessedMetric> findByTenantIdAndWindowStartBetween(
        String tenantId, long from, long to
    );
}
