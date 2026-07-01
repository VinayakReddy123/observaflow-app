package com.observaflow.queryservice.service;

import org.springframework.stereotype.Service;

import com.observaflow.queryservice.model.ProcessedMetric;
import com.observaflow.queryservice.repository.ProcessedMetricRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class MetricsQueryService {
    
    private final ProcessedMetricRepository repository;

    public Flux<ProcessedMetric> queryMetrics(String tenantId, String serviceId, Long from, Long to){
        
        long fromTs = from!=null ? from : 0L;
        long toTs = to!=null ? to : System.currentTimeMillis();

        if(serviceId!=null){
            return repository.findByTenantIdAndServiceIdAndWindowStartBetween(tenantId,serviceId,fromTs,toTs);
        }
        return repository.findByTenantIdAndWindowStartBetween(tenantId, fromTs, toTs);
    }
}
