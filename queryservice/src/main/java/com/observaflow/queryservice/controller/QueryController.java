package com.observaflow.queryservice.controller;

import org.springframework.web.bind.annotation.*;

import com.observaflow.queryservice.model.ProcessedMetric;
import com.observaflow.queryservice.model.TelemetryEvent;
import com.observaflow.queryservice.service.LogQueryService;
import com.observaflow.queryservice.service.MetricsQueryService;
import com.observaflow.queryservice.service.TraceQueryService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/query")
@RequiredArgsConstructor
public class QueryController {

    private final LogQueryService logQueryService;
    private final MetricsQueryService metricsQueryService;
    private final TraceQueryService traceQueryService;

    @GetMapping("/metrics")
    public Flux<ProcessedMetric> getMetrics(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam(required = false) String serviceId,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to) {
        return metricsQueryService.queryMetrics(tenantId, serviceId, from, to);
    }

    @GetMapping("/logs")
    public Flux<TelemetryEvent> getLogs(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String search) {
        return logQueryService.queryLogs(tenantId, level, search);
    }

    @GetMapping("/traces")
    public Flux<TelemetryEvent> getTraces(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam String traceId) {
        return traceQueryService.queryTrace(tenantId, traceId);
    }

    @GetMapping("/traces/recent")
    public Flux<TelemetryEvent> getRecentTraces(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam(defaultValue = "20") int limit){
        return traceQueryService.queryRecentTraces(tenantId, limit);
    }

}
