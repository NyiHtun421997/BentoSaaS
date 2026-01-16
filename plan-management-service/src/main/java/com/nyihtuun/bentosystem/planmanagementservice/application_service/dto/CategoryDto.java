package com.nyihtuun.bentosystem.planmanagementservice.application_service.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CategoryDto {
    private UUID id;
    private final String name;
}
