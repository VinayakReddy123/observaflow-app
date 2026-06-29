package com.observaflow.alert.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.observaflow.alert.model.AlertRule;

import reactor.core.publisher.Flux;



public interface AlertRuleRepository extends ReactiveMongoRepository<AlertRule,String> {
    Flux<AlertRule> findByTenantId(String tenantId);
    Flux<AlertRule> findByTenantIdAndServiceId(String tenantId , String serviceId);
}
