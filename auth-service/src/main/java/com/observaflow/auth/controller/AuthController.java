package com.observaflow.auth.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.observaflow.auth.dto.ApiKeyRequest;
import com.observaflow.auth.dto.ApiKeyResponse;
import com.observaflow.auth.dto.LoginResponse;
import com.observaflow.auth.model.User;
import com.observaflow.auth.repository.ApiKeyRepository;
import com.observaflow.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ApiKeyRepository apiKeyRepository;

    @PostMapping("/register")
    public Mono<String> register(@Valid @RequestBody User user) {
        String email = user.getEmail();
        String password = user.getPassword();
        String tenantId = user.getTenantId();
        return authService.register(email, password, tenantId);
    }

    @PostMapping("/login")
    public Mono<LoginResponse> login(@RequestBody User user) {
        String email = user.getEmail();
        String password = user.getPassword();
        return authService.login(email, password);
    }

    @PostMapping("/refresh")
    public Mono<LoginResponse> refresh(@RequestBody Map<String, String> body) {
        return authService.refresh(body.get("refreshToken"));
    }

    @PostMapping("/generate-api-key")
    public Mono<String> generateApiKey(@RequestBody ApiKeyRequest apiKeyRequest) {
        String name = apiKeyRequest.getName();
        String tenantId = apiKeyRequest.getTenantId();
        return authService.generateApiKey(name, tenantId);
    }

    @GetMapping("/api-keys")
    public Flux<ApiKeyResponse> listApiKeys(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        return apiKeyRepository.findByTenantId(tenantId)
                .map(apiKey -> new ApiKeyResponse(apiKey.getId(), apiKey.getName(), apiKey.getCreatedAt()));
    }

    @DeleteMapping("/api-keys/{id}")
    public Mono<ResponseEntity<Void>> revokeApiKey(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @PathVariable String id) {
        return apiKeyRepository.findById(id)
                .filter(key -> key.getTenantId().equals(tenantId))
                .flatMap(key -> apiKeyRepository.delete(key)
                        .then(Mono.just(ResponseEntity.noContent().<Void>build())))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().<Void>build()));
    }

}
