package com.observaflow.alert.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AlertEvent {

    private String ruleId;
    private String tenantId;
    private String serviceId;

    private String metricType;
    private String operator;
    private String webhookUrl;

    private Double threshold;
    private Double actualValue;

    private long windowStart;
    private long windowEnd;
    private long firedAt;
}
