package com.observaflow.auth.model;


import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "apikeys")
public class ApiKey {

    @Id
    private String id;

    @Indexed(unique = true)
    private String keyHash;
    private String tenantId;

    private String name;
    private Instant createdAt;
}
