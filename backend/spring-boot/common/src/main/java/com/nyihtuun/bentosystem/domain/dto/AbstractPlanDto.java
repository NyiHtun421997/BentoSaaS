package com.nyihtuun.bentosystem.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractPlanDto {
    @NotBlank(message = "{NotBlank.planRequestDto.title}")
    @Size(max = 20, message = "{Size.planRequestDto.title}")
    private String title;

    @NotBlank(message = "{NotBlank.planRequestDto.description}")
    @Size(max = 50, message = "{Size.planRequestDto.description}")
    private String description;

    @NotNull(message = "{NotNull.planRequestDto.categoryIds}")
    @Size(min = 1, message = "{Size.planRequestDto.categoryIds}")
    private Set<@NotNull UUID> categoryIds;

    @NotNull(message = "{NotNull.planRequestDto.address}")
    @Valid
    private AddressDto address;

    @NotNull(message = "{NotNull.planRequestDto.displaySubscriptionFee}")
    @DecimalMin(value = "0.00", inclusive = true, message = "{DecimalMin.planRequestDto.displaySubscriptionFee}")
    @Digits(integer = 10, fraction = 2, message = "{Digits.planRequestDto.displaySubscriptionFee}")
    private BigDecimal displaySubscriptionFee;

    @Size(max = 2, message = "{Size.planRequestDto.skipDays}")
    private List<@NotNull LocalDate> skipDays;

    @Size(max = 255, message = "{Size.planRequestDto.imageUrl}")
    @Pattern(regexp = "^(https?://).*$", message = "{Pattern.planRequestDto.imageUrl}")
    private String imageUrl;
}
