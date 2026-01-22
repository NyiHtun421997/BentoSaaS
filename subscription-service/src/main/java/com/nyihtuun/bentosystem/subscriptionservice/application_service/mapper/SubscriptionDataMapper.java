package com.nyihtuun.bentosystem.subscriptionservice.application_service.mapper;

import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.MealSelectionResponseDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionRequestDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionResponseDto;
import com.nyihtuun.bentosystem.subscriptionservice.domain.entity.MealSelection;
import com.nyihtuun.bentosystem.subscriptionservice.domain.entity.Subscription;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SubscriptionDataMapper {
    public Subscription mapSubscriptionDtoToSubscription(SubscriptionRequestDto subscriptionRequestDto) {
        return Subscription.builder()
                .planId(new PlanId(subscriptionRequestDto.getPlanId()))
                .mealSelections(mapPlanMealIdsToMealSelections(subscriptionRequestDto.getPlanMealIds()))
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
                .build();
    }

    private List<MealSelectionResponseDto> mapMealSelectionsToMealSelectionDtos(List<MealSelection> mealSelections) {
        return mealSelections.stream()
                .map(mealSelection ->
                        MealSelectionResponseDto.builder()
                                .planMealId(mealSelection.getId().planMealId().getValue())
                                .subscriptionId(mealSelection.getId().subscriptionId().getValue())
                                .build())
                .collect(Collectors.toList());
    }
}
