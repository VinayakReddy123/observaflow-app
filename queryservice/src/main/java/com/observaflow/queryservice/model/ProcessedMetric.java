package com.observaflow.queryservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Document(collection = "processed_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedMetric {

    @Id
    private String id;
    
    private String serviceId;
    private String tenantId;
    private double p50;
    private double p95;
    private double p99;
    private long eventCount;
    private long windowStart;
    private long windowEnd;
}
