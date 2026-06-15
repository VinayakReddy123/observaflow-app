package com.observaflow.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.observaflow.auth.dto.ApiKeyRequest;
import com.observaflow.auth.model.User;
import com.observaflow.auth.service.AuthService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Mono<String> register(@RequestBody User user) {
        String email = user.getEmail();
        String password = user.getPassword();
        String tenantId = user.getTenantId();
        return authService.register(email, password, tenantId);
    }

    @PostMapping("/login")
    public Mono<String> login(@RequestBody User user) {
        String email = user.getEmail();
        String password = user.getPassword();
        return authService.login(email, password);
    }

    @PostMapping("/generate-api-key")
    public Mono<String> generateApiKey(@RequestBody ApiKeyRequest apiKeyRequest) {
        String name = apiKeyRequest.getName();
        String tenantId = apiKeyRequest.getTenantId();
        return authService.generateApiKey(name, tenantId);
    }

}
