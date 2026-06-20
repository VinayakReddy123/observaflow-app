package com.observaflow.ingest.model;

import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "metrics")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TelemetryEvent {

    @NotBlank(message = "ServiceId is required")
    private String serviceId;

    @NotBlank(message = "TenantId is required")
    private String tenantId;

    @NotBlank(message = "EventType is required")
    private EventType type;

    private Map<String, Object> payload;
    private long timestamp;

    public enum EventType {
        METRIC, LOG, TRACE
    }
}
