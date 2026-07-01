package com.observaflow.queryservice.service;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.observaflow.queryservice.model.TelemetryEvent;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class TraceQueryService {

    private final ReactiveMongoTemplate mongoTemplate;

    public Flux<TelemetryEvent> queryTrace(String tenantId, String traceId) {
        Criteria criteria = Criteria.where("tenantId").is(tenantId)
                                    .and("type").is("TRACE")
                                    .and("payload.traceId").is(traceId);

        Query query = Query.query(criteria)
                           .with(Sort.by("timestamp").ascending());

        return mongoTemplate.find(query, TelemetryEvent.class, "raw_events");
    }
}
