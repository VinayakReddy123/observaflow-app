package com.observaflow.auth.repository;


import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.observaflow.auth.model.User;

import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {
   Mono<User> findByEmail(String email);
} 
