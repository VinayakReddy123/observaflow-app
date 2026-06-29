package com.observaflow.auth.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.observaflow.auth.model.ApiKey;
import com.observaflow.auth.model.User;
import com.observaflow.auth.repository.ApiKeyRepository;
import com.observaflow.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final ApiKeyRepository apiKeyRepository;

    public Mono<String> register(String email, String password, String tenantId) {
        // Implementation for registering a new user
        String hashedPassword = passwordEncoder.encode(password);
        User user = User.builder()
                .email(email)
                .password(hashedPassword)
                .tenantId(tenantId)
                .roles(List.of("USER"))
                .build();

        return userRepository.save(user)
                .map(savedUser -> jwtService.generateToken(savedUser.getId(), savedUser.getTenantId(), "USER"));
    }

    public Mono<String> login(String email, String password) {
        return userRepository.findByEmail(email)
            .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
            .flatMap(user -> {
                if (!passwordEncoder.matches(password, user.getPassword())) {
                    return Mono.error(new RuntimeException("Invalid credentials"));
                }
                return Mono.just(jwtService.generateToken(user.getId(), user.getTenantId(), "USER"));
            });
    }


    public Mono<String> generateApiKey(String name, String tenantId) {
        // Implementation for generating an API key for a user
        String plainKey = UUID.randomUUID().toString();
        String hashedKey = passwordEncoder.encode(plainKey);
        ApiKey apiKey = ApiKey.builder()
                .keyHash(hashedKey)
                .name(name)
                .tenantId(tenantId)
                .createdAt(Instant.now())
                .build();
        return apiKeyRepository.save(apiKey)
                .map(savedApiKey -> plainKey);
    }

}
