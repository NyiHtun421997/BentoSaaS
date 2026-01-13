package com.nyihtuun.bentosystem.planmanagementservice.application_service.dto;

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
    @NotBlank
    @Size(max = 20)
    private String title;

    @NotBlank
    @Size(max = 50)
    private String description;

    @NotNull
    @Size(min = 1)
    private Set<@NotNull UUID> categoryIds;

    @NotNull
    @Valid
    private AddressDto address;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = true)
    @Digits(integer = 10, fraction = 2)
    private BigDecimal displaySubscriptionFee;

    @Size(max = 2)
    private List<@NotNull LocalDate> skipDays;
}
