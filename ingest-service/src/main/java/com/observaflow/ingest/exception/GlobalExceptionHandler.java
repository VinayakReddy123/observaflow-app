package com.observaflow.ingest.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MeterRegistry meterRegistry;

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<String> handleValidationError(WebExchangeBindException ex) {
        meterRegistry.counter("validation_failures_total").increment();
        return ResponseEntity.badRequest().body("Validation failed: " + ex.getMessage());
    }
}
