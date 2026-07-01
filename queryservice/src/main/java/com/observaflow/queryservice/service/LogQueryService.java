package com.observaflow.queryservice.service;

import org.bson.Document;
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
public class LogQueryService {
    
    private final ReactiveMongoTemplate mongoTemplate;

    public Flux<TelemetryEvent> queryLogs(String tenantId, String level, String search){
        Criteria criteria = Criteria.where("tenantId").is(tenantId)
                              .and("type").is("LOG");

        if (level != null) {
            criteria = criteria.and("payload.level").is(level);
        }

        if (search != null) {
            criteria = criteria.and("$text").is(new Document("$search", search));
        }

        Query query = Query.query(criteria)
                           .with(Sort.by("timestamp").descending())
                           .limit(100);

        return mongoTemplate.find(query, TelemetryEvent.class, "raw_events");
    }
}
