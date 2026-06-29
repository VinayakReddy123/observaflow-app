package com.observaflow.processor.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryEvent {
    private String serviceId;
    private String tenantId;
    private EventType type;
    private Map<String, Object> payload;
    private long timestamp;

    public enum EventType {
        METRIC, LOG, TRACE
    }
}
