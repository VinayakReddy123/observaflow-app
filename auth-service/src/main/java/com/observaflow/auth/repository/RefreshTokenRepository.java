package com.observaflow.auth.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.observaflow.auth.model.RefreshToken;

import reactor.core.publisher.Mono;

public interface RefreshTokenRepository extends ReactiveMongoRepository<RefreshToken,String> {
    Mono<RefreshToken> findByToken(String token);
    Mono<Void> deleteByUserId(String userId);
}
