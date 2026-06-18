package com.observaflow.gateway.filter;

import java.time.Duration;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter implements GlobalFilter, Ordered {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final WebClient.Builder webClientBuilder;

    private static final String CACHE_PREFIX = "apikey:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/register",
            "/auth/login");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip for public paths
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        // If Bearer token present → JwtAuthFilter already handled it, skip here
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        // Read API key from X-API-Key header
        String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");

        if (apiKey == null || apiKey.isBlank()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Check Redis cache first
        return redisTemplate.opsForValue().get(CACHE_PREFIX + apiKey)
                .flatMap(cachedTenantId ->
                    // Cache hit — tenantId already in Redis, just forward
                    forwardWithTenantId(exchange, chain, cachedTenantId)
                )
                .switchIfEmpty(
                    // Cache miss — call auth-service to validate the key
                    validateWithAuthService(apiKey)
                            .flatMap(tenantId ->
                                // Valid key — store in Redis then forward
                                redisTemplate.opsForValue()
                                        .set(CACHE_PREFIX + apiKey, tenantId, CACHE_TTL)
                                        .then(forwardWithTenantId(exchange, chain, tenantId))
                            )
                            .onErrorResume(e -> {
                                // Key invalid or auth-service unreachable → 401
                                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                return exchange.getResponse().setComplete();
                            })
                );
    }

    private Mono<Void> forwardWithTenantId(ServerWebExchange exchange,
                                            GatewayFilterChain chain,
                                            String tenantId) {
        ServerWebExchange mutated = exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .header("X-Tenant-Id", tenantId)
                        .build())
                .build();
        return chain.filter(mutated);
    }

    private Mono<String> validateWithAuthService(String apiKey) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/auth/validate-key?key=" + apiKey)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                        response -> Mono.error(new RuntimeException("Invalid API key")))
                .bodyToMono(String.class);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
