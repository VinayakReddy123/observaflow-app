package com.observaflow.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedMetric {

    private String serviceId;
    private String tenantId;
    private double p50;
    private double p95;
    private double p99;
    private long eventCount;
    private long windowStart;
    private long windowEnd;
}
