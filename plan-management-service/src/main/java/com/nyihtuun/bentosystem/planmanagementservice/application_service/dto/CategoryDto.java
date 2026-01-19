package com.nyihtuun.bentosystem.planmanagementservice.application_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CategoryDto {
    private UUID id;

    @NotBlank
    @Size(min = 2, max = 50)
    private String name;

    public CategoryDto(String name) {
        this.name = name;
    }

    public CategoryDto(UUID id, String name) {
        this.id = id;
        this.name = name;
    }
}
