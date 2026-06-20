package com.observaflow.ingest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.observaflow.ingest.model.TelemetryEvent;
import com.observaflow.ingest.service.KafkaProducerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class IngestController {

    private final KafkaProducerService kafkaProducerService;

    @PostMapping("/metrics")
    public Mono<ResponseEntity<Void>> postMetrics(@Valid @RequestBody TelemetryEvent event) {
       return kafkaProducerService.publish(event)
       .then( Mono.just(new ResponseEntity<>(HttpStatus.ACCEPTED)));
    }

    @PostMapping("/logs")
    public Mono<ResponseEntity<Void>> postLogs(@Valid @RequestBody TelemetryEvent event) {
        return kafkaProducerService.publish(event)
       .then( Mono.just(new ResponseEntity<>(HttpStatus.ACCEPTED)));
    }

    @PostMapping("/traces")
    public Mono<ResponseEntity<Void>> postTraces(@Valid @RequestBody TelemetryEvent event) {
       return kafkaProducerService.publish(event)
       .then( Mono.just(new ResponseEntity<>(HttpStatus.ACCEPTED)));
    }

}
