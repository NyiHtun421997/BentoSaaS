package com.nyihtuun.bentosystem.subscriptionservice.data_access.jpa_entity;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealSelectionEntityId implements Serializable {
    private UUID subscription;
    private UUID planMealId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MealSelectionEntityId that = (MealSelectionEntityId) o;
        return Objects.equals(subscription, that.subscription) &&
                Objects.equals(planMealId, that.planMealId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscription, planMealId);
    }

    @Override
    public String toString() {
        return "MealSelectionEntityId{" +
                "subscription=" + subscription +
                ", planMealId=" + planMealId +
                '}';
    }
}
