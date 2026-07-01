package com.observaflow.queryservice.model;

import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "raw_events")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TelemetryEvent {

    @Id
    private String id;

    private String serviceId;

    private String tenantId;

    private EventType type;

    private Map<String, Object> payload;
    private long timestamp;

    public enum EventType {
        METRIC, LOG, TRACE
    }
}
