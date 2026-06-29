package com.observaflow.alert.service;

import org.springframework.stereotype.Service;

import com.observaflow.alert.model.AlertEvent;
import com.observaflow.alert.model.AlertRule;
import com.observaflow.alert.model.ProcessedMetric;
import com.observaflow.alert.model.AlertRule.MetricType;
import com.observaflow.alert.repository.AlertRuleRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class AlertEvaluatorService {
    
    private final AlertRuleRepository alertRepository;
    public Flux<AlertEvent> evaluate(ProcessedMetric metric){
        return alertRepository.findByTenantIdAndServiceId(metric.getTenantId(), metric.getServiceId())
               .filter(AlertRule::isEnabled)
               .filter(rule->shouldFire(rule,metric))
               .map(rule->buildEvent(rule,metric));
    }

    private boolean shouldFire(AlertRule rule , ProcessedMetric metric){
        double actual = extractValue(metric,rule.getMetricType());
        double threshold = rule.getThreshold();

        return switch(rule.getOperator()){
            case GT -> actual > threshold;
            case LT -> actual < threshold;
            case GTE -> actual >= threshold;
            case LTE -> actual <= threshold;
        };
    }

    private double extractValue(ProcessedMetric metric, MetricType metricType){
        return switch(metricType){
            case P50 -> metric.getP50();
            case P95 -> metric.getP95();
            case P99 -> metric.getP99();
        };
    }

    private AlertEvent buildEvent(AlertRule rule,ProcessedMetric metric){
         return AlertEvent.builder()
                .ruleId(rule.getId())
                .tenantId(rule.getTenantId())
                .serviceId(rule.getServiceId())
                .metricType(rule.getMetricType().name())
                .operator(rule.getOperator().name())
                .threshold(rule.getThreshold())
                .actualValue(extractValue(metric, rule.getMetricType()))
                .windowStart(metric.getWindowStart())
                .windowEnd(metric.getWindowEnd())
                .webhookUrl(rule.getWebhookUrl())
                .firedAt(System.currentTimeMillis())
                .build();
    }
}
