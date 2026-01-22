package com.nyihtuun.bentosystem.subscriptionservice.data_access.mapper;

import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import com.nyihtuun.bentosystem.domain.valueobject.SubscriptionId;
import com.nyihtuun.bentosystem.domain.valueobject.UserId;
import com.nyihtuun.bentosystem.subscriptionservice.data_access.jpa_entity.MealSelectionEntity;
import com.nyihtuun.bentosystem.subscriptionservice.data_access.jpa_entity.SubscriptionEntity;
import com.nyihtuun.bentosystem.subscriptionservice.domain.entity.MealSelection;
import com.nyihtuun.bentosystem.subscriptionservice.domain.entity.Subscription;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SubscriptionDataAccessMapper {

    public SubscriptionEntity subscriptionToSubscriptionEntity(Subscription subscription) {
        SubscriptionEntity subscriptionEntity = SubscriptionEntity.builder()
                                                                  .id(subscription.getId().getValue())
                                                                  .userId(subscription.getUserId().getValue())
                                                                  .planId(subscription.getPlanId().getValue())
                                                                  .providedUserId(subscription.getProvidedUserId().getValue())
                                                                  .subscriptionStatus(subscription.getSubscriptionStatus())
                                                                  .appliedAt(subscription.getAppliedAt())
                                                                  .updatedAt(subscription.getUpdatedAt())
                                                                  .cancelledAt(subscription.getCancelledAt())
                                                                  .activatedAt(subscription.getActivatedAt())
                                                                  .mealSelections(mealSelectionsToMealSelectionEntities(subscription.getMealSelections()))
                                                                  .build();

        if (subscriptionEntity.getMealSelections() != null) {
            subscriptionEntity.getMealSelections().forEach(mealSelectionEntity ->
                                                                   mealSelectionEntity.setSubscription(subscriptionEntity));
        }

        return subscriptionEntity;
    }

    public Subscription subscriptionEntityToSubscription(SubscriptionEntity subscriptionEntity) {
        return Subscription.builder()
                           .subscriptionId(new SubscriptionId(subscriptionEntity.getId()))
                           .userId(new UserId(subscriptionEntity.getUserId()))
                           .planId(new PlanId(subscriptionEntity.getPlanId()))
                           .providedUserId(new UserId(subscriptionEntity.getProvidedUserId()))
                           .subscriptionStatus(subscriptionEntity.getSubscriptionStatus())
                           .appliedAt(subscriptionEntity.getAppliedAt())
                           .updatedAt(subscriptionEntity.getUpdatedAt())
                           .cancelledAt(subscriptionEntity.getCancelledAt())
                           .activatedAt(subscriptionEntity.getActivatedAt())
                           .mealSelections(mealSelectionEntitiesToMealSelections(subscriptionEntity.getMealSelections()))
                           .build();
    }

    private List<MealSelectionEntity> mealSelectionsToMealSelectionEntities(List<MealSelection> mealSelections) {
        if (mealSelections == null) {
            return new ArrayList<>();
        }
        return mealSelections.stream()
                             .map(mealSelection -> MealSelectionEntity.builder()
                                                                      .planMealId(mealSelection.getId().planMealId().getValue())
                                                                      .build())
                             .collect(Collectors.toList());
    }

    private List<MealSelection> mealSelectionEntitiesToMealSelections(List<MealSelectionEntity> mealSelectionEntities) {
        if (mealSelectionEntities == null) {
            return new ArrayList<>();
        }
        return mealSelectionEntities.stream()
                                    .map(mealSelectionEntity -> MealSelection.builder()
                                                                             .subscriptionId(new SubscriptionId(mealSelectionEntity.getSubscription()
                                                                                                                                   .getId()))
                                                                             .planMealId(new PlanMealId(mealSelectionEntity.getPlanMealId()))
                                                                             .build())
                                    .collect(Collectors.toCollection(ArrayList::new));
    }
}
