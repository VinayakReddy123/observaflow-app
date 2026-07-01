package com.observaflow.queryservice.config;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.data.mongodb.core.index.TextIndexDefinition.TextIndexDefinitionBuilder;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MongoIndexConfig {

    private final ReactiveMongoTemplate mongoTemplate;

    @PostConstruct
    public void createIndexes() {
        TextIndexDefinition textIndex = new TextIndexDefinitionBuilder()
                .onField("payload.message")
                .build();

        mongoTemplate.indexOps("raw_events")
                .ensureIndex(textIndex)
                .doOnSuccess(name -> log.info("Text index created: {}", name))
                .subscribe();

        mongoTemplate.indexOps("raw_events")
                .ensureIndex(new Index()
                        .on("timestamp", Direction.ASC)
                        .expire(7, TimeUnit.DAYS))
                .doOnSuccess(name -> log.info("TTL index created: {}", name))
                .subscribe();
    }
}
