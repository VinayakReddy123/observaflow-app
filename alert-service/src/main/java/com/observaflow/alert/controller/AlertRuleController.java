package com.observaflow.alert.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.observaflow.alert.model.AlertRule;
import com.observaflow.alert.repository.AlertRuleRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
public class AlertRuleController {

    private final AlertRuleRepository ruleRepository;

    @PostMapping
    public Mono<ResponseEntity<AlertRule>> create(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @Valid @RequestBody AlertRule rule) {
        rule.setTenantId(tenantId);
        return ruleRepository.save(rule)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @GetMapping
    public Flux<AlertRule> list(@RequestHeader("X-Tenant-Id") String tenantId) {
        return ruleRepository.findByTenantId(tenantId);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @PathVariable String id) {
        return ruleRepository.findById(id)
                .filter(rule -> rule.getTenantId().equals(tenantId))
                .flatMap(rule -> ruleRepository.delete(rule)
                        .then(Mono.just(ResponseEntity.noContent().<Void>build())))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<AlertRule>> update(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @PathVariable String id,
            @Valid @RequestBody AlertRule rule) {
        return ruleRepository.findById(id)
                .filter(exist -> exist.getTenantId().equals(tenantId))
                .flatMap(exist -> {
                    rule.setId(id);
                    rule.setTenantId(tenantId);
                    return ruleRepository.save(rule);
                })
                .map(saved -> ResponseEntity.ok(saved))
                .switchIfEmpty(Mono.just(ResponseEntity.<AlertRule>notFound().<AlertRule>build()));

    }
}
