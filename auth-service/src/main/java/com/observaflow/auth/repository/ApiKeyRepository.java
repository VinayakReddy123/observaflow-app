package com.observaflow.auth.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.observaflow.auth.model.ApiKey;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ApiKeyRepository extends ReactiveMongoRepository<ApiKey, String> {
    Mono<ApiKey> findByKeyHash(String keyHash);

    Flux<ApiKey> findByTenantId(String tenantId);
}
