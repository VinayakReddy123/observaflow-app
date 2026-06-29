package com.observaflow.alert.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "alert_rules")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlertRule {

    @Id
    private String id;

    @NotBlank
    private String tenantId;
    @NotBlank
    private String serviceId;
    @NotBlank
    private String webhookUrl;
    @NotNull
    private Double threshold;

    @Builder.Default
    private boolean enabled = true;

    @NotNull
    private MetricType metricType;

    @NotNull
    private Operator operator;

    public enum MetricType {
        P50, P95, P99
    }

    public enum Operator {
        GT, LT, GTE, LTE
    }

}
