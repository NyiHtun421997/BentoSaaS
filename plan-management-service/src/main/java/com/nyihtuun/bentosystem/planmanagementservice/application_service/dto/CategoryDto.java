package com.nyihtuun.bentosystem.planmanagementservice.application_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CategoryDto {
    private UUID id;

    @NotBlank(message = "{NotBlank.categoryDto.name}")
    @Size(max = 50, message = "{Size.categoryDto.name}")
    private String name;

    public CategoryDto(String name) {
        this.name = name;
    }

    public CategoryDto(UUID id, String name) {
        this.id = id;
        this.name = name;
    }
}
