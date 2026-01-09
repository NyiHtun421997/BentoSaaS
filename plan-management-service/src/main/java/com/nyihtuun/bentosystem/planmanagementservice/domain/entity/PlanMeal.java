package com.nyihtuun.bentosystem.planmanagementservice.domain.entity;

import com.nyihtuun.bentosystem.domain.entity.BaseEntity;
import com.nyihtuun.bentosystem.domain.valueobject.Money;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import com.nyihtuun.bentosystem.domain.valueobject.Threshold;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
public class PlanMeal extends BaseEntity<PlanMealId> {
    private String name;
    private String description;
    private Money price;

    private boolean isPrimary;
    private Threshold minSubCount;
    private int currentSubCount;

    private boolean isActive;
    private String imageUrl;

    private final ZonedDateTime createdAt;

    @Setter
    private ZonedDateTime updatedAt;

    private PlanMeal(Builder builder) {
        super.setId(builder.planMealId);
        name = builder.name;
        description = builder.description;
        price = builder.price;
        isPrimary = builder.isPrimary;
        minSubCount = builder.minSubCount;
        currentSubCount = builder.currentSubCount;
        isActive = builder.isActive;
        imageUrl = builder.imageUrl;
        createdAt = builder.createdAt;
        updatedAt = builder.updatedAt;
    }

    protected void validateMeal() {
        // validation logic will be implemented later
    }


    public static final class Builder {
        private PlanMealId planMealId;
        private String name;
        private String description;
        private Money price;
        private boolean isPrimary;
        private Threshold minSubCount;
        private int currentSubCount;
        private boolean isActive;
        private String imageUrl;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder planMealId(PlanMealId val) {
            planMealId = val;
            return this;
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder description(String val) {
            description = val;
            return this;
        }

        public Builder price(Money val) {
            price = val;
            return this;
        }

        public Builder isPrimary(boolean val) {
            isPrimary = val;
            return this;
        }

        public Builder minSubCount(Threshold val) {
            minSubCount = val;
            return this;
        }

        public Builder currentSubCount(int val) {
            currentSubCount = val;
            return this;
        }

        public Builder isActive(boolean val) {
            isActive = val;
            return this;
        }

        public Builder imageUrl(String val) {
            imageUrl = val;
            return this;
        }

        public Builder createdAt(ZonedDateTime val) {
            createdAt = val;
            return this;
        }

        public Builder updatedAt(ZonedDateTime val) {
            updatedAt = val;
            return this;
        }

        public PlanMeal build() {
            return new PlanMeal(this);
        }
    }
}
