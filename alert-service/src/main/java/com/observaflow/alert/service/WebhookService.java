package com.observaflow.alert.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.observaflow.alert.model.AlertEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {
    
    private final WebClient.Builder webClientBuilder;

    
    public Mono<Void> fire(String webhookUrl,AlertEvent event){
         return webClientBuilder.build()
                .post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(event)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(ignored -> log.info("Webhook delivered successfully to: {}", webhookUrl))
                .doOnError(error -> log.error("Webhook delivery failed to: {} | reason: {}", webhookUrl, error.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }
}
