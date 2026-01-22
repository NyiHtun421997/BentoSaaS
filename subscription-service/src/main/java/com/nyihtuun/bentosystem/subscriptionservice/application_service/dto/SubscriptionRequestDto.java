package com.nyihtuun.bentosystem.subscriptionservice.application_service.dto;

import jakarta.validation.constraints.NotNull;
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
public class SubscriptionRequestDto implements Serializable {
    @NonNull
    private UUID planId;

    @NotNull
    private List<@NotNull UUID> planMealIds;

    @NotNull
    private UUID providedUserId;
}
