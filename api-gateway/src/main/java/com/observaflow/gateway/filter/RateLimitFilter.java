package com.observaflow.gateway.filter;

import java.util.UUID;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final long MAX_REQUESTS = 1000L;
    private static final long WINDOW_MS = 60_000L; // 1 minute in milliseconds
    private static final String RATE_LIMIT_PREFIX = "ratelimit:";

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Override
   public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Only rate limit API key requests — JWT users skip this
        String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
        if (apiKey == null || apiKey.isBlank()) {
            return chain.filter(exchange);
        }

        String redisKey = RATE_LIMIT_PREFIX + apiKey;
        long now = System.currentTimeMillis();
        long windowStart = now - WINDOW_MS; // everything before this is outside the window

        return redisTemplate.opsForZSet()
                // Step 1: remove entries older than 1 minute
                .removeRangeByScore(redisKey, Range.closed(0.0, (double) windowStart))
                .flatMap(removed ->
                    // Step 2: count requests still in the window
                    redisTemplate.opsForZSet().count(redisKey, Range.closed((double) windowStart, (double) now))
                )
                .flatMap(count -> {
                    if (count >= MAX_REQUESTS) {
                        // Step 3a: limit exceeded → 429 Too Many Requests
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        return exchange.getResponse().setComplete();
                    }
                    // Step 3b: under limit → record this request and allow it
                    return redisTemplate.opsForZSet()
                            .add(redisKey, UUID.randomUUID().toString(), now)
                            .then(chain.filter(exchange));
                });
    }

   @Override
   public int getOrder() {
      return 1;
   }
}