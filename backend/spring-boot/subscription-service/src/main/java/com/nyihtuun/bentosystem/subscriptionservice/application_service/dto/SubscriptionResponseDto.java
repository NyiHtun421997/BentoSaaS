package com.nyihtuun.bentosystem.subscriptionservice.application_service.dto;

import com.nyihtuun.bentosystem.domain.valueobject.status.SubscriptionStatus;
import com.nyihtuun.bentosystem.subscriptionservice.domain.entity.MealSelection;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class SubscriptionResponseDto implements Serializable {
    private UUID subscriptionId;
    private UUID userId;
    private UUID planId;
    private SubscriptionStatus subscriptionStatus;
    private List<MealSelectionResponseDto> mealSelectionResponseDtos;
    private UUID providedUserId;
}
