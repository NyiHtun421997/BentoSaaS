package com.nyihtuun.bentosystem.subscriptionservice.application_service.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.UUID;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class MealSelectionResponseDto implements Serializable {
    private UUID subscriptionId;
    private UUID planMealId;
}
