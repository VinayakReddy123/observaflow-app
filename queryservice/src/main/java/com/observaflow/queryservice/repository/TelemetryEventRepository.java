package com.observaflow.queryservice.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.observaflow.queryservice.model.TelemetryEvent;

public interface TelemetryEventRepository extends ReactiveMongoRepository<TelemetryEvent,String> {
    
    
}
