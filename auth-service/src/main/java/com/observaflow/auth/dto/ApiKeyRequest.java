package com.observaflow.auth.dto;

import lombok.Data;

@Data

public class ApiKeyRequest {

    private String name;
    private String tenantId;
}
