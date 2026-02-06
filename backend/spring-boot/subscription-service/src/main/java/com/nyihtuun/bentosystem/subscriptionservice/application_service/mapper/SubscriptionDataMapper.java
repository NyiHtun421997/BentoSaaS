package com.nyihtuun.bentosystem.subscriptionservice.application_service.mapper;

import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import com.nyihtuun.bentosystem.domain.valueobject.UserId;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.MealSelectionResponseDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionResponseDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client.PlanData;
import com.nyihtuun.bentosystem.subscriptionservice.domain.entity.MealSelection;
import com.nyihtuun.bentosystem.subscriptionservice.domain.entity.Subscription;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SubscriptionDataMapper {
    public Subscription mapToSubscription(PlanData planData, UserId providedUserId) {
        return Subscription.builder()
                .planId(new PlanId(planData.planId()))
                .mealSelections(mapPlanMealIdsToMealSelections(planData.planMealIds()))
                .providedUserId(providedUserId)
                .build();
    }

    private List<MealSelection> mapPlanMealIdsToMealSelections(List<UUID> planMealIds) {
        return planMealIds.stream()
                .map(planMealId ->
                        MealSelection.builder()
                                .planMealId(new PlanMealId(planMealId))
                                .build())
                .toList();
    }

    public SubscriptionResponseDto mapSubscriptionToSubscriptionDto(Subscription subscription) {
        return SubscriptionResponseDto.builder()
                .subscriptionId(subscription.getId().getValue())
                .planId(subscription.getPlanId().getValue())
                .userId(subscription.getUserId().getValue())
                .subscriptionStatus(subscription.getSubscriptionStatus())
                .mealSelectionResponseDtos(mapMealSelectionsToMealSelectionDtos(subscription.getMealSelections()))
                .providedUserId(subscription.getProvidedUserId().getValue())
                .build();
    }

    private List<MealSelectionResponseDto> mapMealSelectionsToMealSelectionDtos(List<MealSelection> mealSelections) {
        return mealSelections.stream()
                .map(mealSelection ->
                        MealSelectionResponseDto.builder()
                                .planMealId(mealSelection.getId().getPlanMealId().getValue())
                                .subscriptionId(mealSelection.getId().getSubscriptionId().getValue())
                                .build())
                .collect(Collectors.toList());
    }
}
