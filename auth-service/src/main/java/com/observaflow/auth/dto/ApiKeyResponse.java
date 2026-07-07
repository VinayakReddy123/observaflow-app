package com.observaflow.auth.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiKeyResponse {
    private String id;
    private String name;
    private Instant createdAt;
}
