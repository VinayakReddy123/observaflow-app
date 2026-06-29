package com.observaflow.gateway.filter;

import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.observaflow.gateway.service.JwtService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;

    // These paths don't need a token — anyone can call them
    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/register",
            "/auth/login");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip validation for public endpoints
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        // Read Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        // No header or doesn't start with "Bearer " → 401
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        // Extract the token (remove "Bearer " prefix)
        String token = authHeader.substring(7);

        // Invalid or expired token → 401
        if (!jwtService.isTokenValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Token valid — extract tenantId and forward as header to downstream service
        String tenantId = jwtService.extractTenantId(token);

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .header("X-Tenant-Id", tenantId)
                        .build())
                .build();

        return chain.filter(mutatedExchange);
    }

    // Lower number = higher priority — this filter runs first
    @Override
    public int getOrder() {
        return -1;
    }
}
