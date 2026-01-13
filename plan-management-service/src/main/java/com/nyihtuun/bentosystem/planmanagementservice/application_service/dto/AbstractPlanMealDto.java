package com.nyihtuun.bentosystem.planmanagementservice.application_service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractPlanMealDto {
    @NotBlank
    @Size(max = 20)
    private String name;

    @NotBlank
    @Size(max = 50)
    private String description;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = true)
    @Digits(integer = 10, fraction = 2)
    private BigDecimal pricePerMonth;

    private boolean isPrimary;

    @Min(0)
    private int minSubCount;

    @Size(max = 255)
    @Pattern(
            regexp = "^(https?://).*$"
    )
    private String imageUrl;
}
