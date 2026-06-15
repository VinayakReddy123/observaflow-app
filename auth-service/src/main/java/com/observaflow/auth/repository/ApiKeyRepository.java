package com.observaflow.auth.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.observaflow.auth.model.ApiKey;

import reactor.core.publisher.Mono;

public interface ApiKeyRepository extends ReactiveMongoRepository<ApiKey, String> {
    Mono<ApiKey> findByKeyHash(String keyHash);
}
