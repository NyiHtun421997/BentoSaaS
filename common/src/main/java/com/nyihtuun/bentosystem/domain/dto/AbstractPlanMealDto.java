package com.nyihtuun.bentosystem.domain.dto;

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
    @NotBlank(message = "{NotBlank.planMealRequestDto.name}")
    @Size(max = 20, message = "{Size.planMealRequestDto.name}")
    private String name;

    @NotBlank(message = "{NotBlank.planMealRequestDto.description}")
    @Size(max = 50, message = "{Size.planMealRequestDto.description}")
    private String description;

    @NotNull(message = "{NotNull.planMealRequestDto.pricePerMonth}")
    @DecimalMin(value = "0.00", inclusive = true, message = "{DecimalMin.planMealRequestDto.pricePerMonth}")
    @Digits(integer = 10, fraction = 2, message = "{Digits.planMealRequestDto.pricePerMonth}")
    private BigDecimal pricePerMonth;

    private boolean primary;

    @Min(value = 0, message = "{Min.planMealRequestDto.minSubCount}")
    private int minSubCount;

    @Size(max = 255, message = "{Size.planMealRequestDto.imageUrl}")
    @Pattern(regexp = "^(https?://).*$", message = "{Pattern.planMealRequestDto.imageUrl}")
    private String imageUrl;
}
