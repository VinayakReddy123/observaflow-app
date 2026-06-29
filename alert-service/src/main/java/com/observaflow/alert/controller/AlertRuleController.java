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
        @RequestHeader("X-Tenant-Id") String tenantId ,
        @Valid @RequestBody AlertRule rule
    ){
       rule.setTenantId(tenantId);
       return ruleRepository.save(rule)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @GetMapping
    public Flux<AlertRule> list(@RequestHeader("X-Tenant-Id") String tenantId){
        return ruleRepository.findByTenantId(tenantId);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @PathVariable String id) {
        return ruleRepository.deleteById(id)
            .then(Mono.just(ResponseEntity.<Void>noContent().build()));
    }
}
