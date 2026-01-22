package com.nyihtuun.bentosystem.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MealSelectionId {
    SubscriptionId subscriptionId;
    PlanMealId planMealId;
}
