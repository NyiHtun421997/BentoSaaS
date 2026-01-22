package com.nyihtuun.bentosystem.subscriptionservice.domain.entity;

import com.nyihtuun.bentosystem.domain.entity.BaseEntity;
import com.nyihtuun.bentosystem.domain.valueobject.MealSelectionId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import com.nyihtuun.bentosystem.domain.valueobject.SubscriptionId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class MealSelection extends BaseEntity<MealSelectionId> {
    private MealSelection(Builder builder) {
        super.setId(new MealSelectionId(builder.subscriptionId, builder.planMealId));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private PlanMealId planMealId;
        private SubscriptionId subscriptionId;

        private Builder() {
        }

        public Builder planMealId(PlanMealId val) {
            planMealId = val;
            return this;
        }

        public Builder subscriptionId(SubscriptionId val) {
            subscriptionId = val;
            return this;
        }

        public MealSelection build() {
            return new MealSelection(this);
        }
    }
}
