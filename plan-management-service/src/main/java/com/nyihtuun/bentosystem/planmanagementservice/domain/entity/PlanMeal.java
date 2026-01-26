package com.nyihtuun.bentosystem.planmanagementservice.domain.entity;

import com.nyihtuun.bentosystem.domain.entity.BaseEntity;
import com.nyihtuun.bentosystem.domain.valueobject.*;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementDomainException;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementErrorCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@ToString
public class PlanMeal extends BaseEntity<PlanMealId> {
    private PlanId planId;
    private String name;
    private String description;
    private Money pricePerMonth;

    private boolean isPrimary;
    private Threshold minSubCount;
    private int currentSubCount;

    private String imageUrl;

    private LocalDateTime createdAt;

    @Setter
    private LocalDateTime updatedAt;

    private boolean deleteFlag;
    private LocalDateTime deletedAt;

    private PlanMeal(Builder builder) {
        super.setId(builder.planMealId);
        planId = builder.planId;
        name = builder.name;
        description = builder.description;
        pricePerMonth = builder.price;
        isPrimary = builder.isPrimary;
        minSubCount = builder.minSubCount;
        currentSubCount = builder.currentSubCount;
        imageUrl = builder.imageUrl;
        createdAt = builder.createdAt;
        updatedAt = builder.updatedAt;
        deleteFlag = builder.deleteFlag;
        deletedAt = builder.deletedAt;
    }

     void validateMeal() {
        if (!pricePerMonth.isGreaterThanZero())
            throw new PlanManagementDomainException(PlanManagementErrorCode.NEGATIVE_PLANMEAL_PRICE);
        if (minSubCount.isNegative())
            throw new PlanManagementDomainException(PlanManagementErrorCode.NEGATIVE_PLANMEAL_MINSUBCOUNT);
        if (isPrimary && !minSubCount.isGreaterThanZero())
            throw new PlanManagementDomainException(PlanManagementErrorCode.PRIMARYMEAL_INVALID_THRESHOLD);
        if (!imageUrl.startsWith("https"))
            throw new PlanManagementDomainException(PlanManagementErrorCode.INVALID_IMAGE_URL);
    }

    void initializeMeal(PlanId planId) {
        super.setId(new PlanMealId(UUID.randomUUID()));
        this.planId = planId;
        this.currentSubCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deleteFlag = false;
    }

    void reflectUserSelection(boolean apply) {
        if (apply) {
            this.currentSubCount++;
            this.updatedAt = LocalDateTime.now();
            return;
        }
        if (this.currentSubCount > 0) {
            this.currentSubCount--;
            this.updatedAt = LocalDateTime.now();
        }
    }

    void deleteMeal() {
        this.deleteFlag = true;
        this.deletedAt = LocalDateTime.now();
    }

    void updateMeal(PlanMealUpdateCommand planMealUpdateCommand) {
        this.name = planMealUpdateCommand.getName();
        this.description = planMealUpdateCommand.getDescription();
        this.pricePerMonth = planMealUpdateCommand.getPricePerMonth();
        this.isPrimary = planMealUpdateCommand.isPrimary();
        this.minSubCount = planMealUpdateCommand.getMinSubCount();
        this.imageUrl = planMealUpdateCommand.getImageUrl();
        this.updatedAt = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private PlanMealId planMealId;
        private PlanId planId;
        private String name;
        private String description;
        private Money price;
        private boolean isPrimary;
        private Threshold minSubCount;
        private int currentSubCount;
        private String imageUrl;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean deleteFlag;
        private LocalDateTime deletedAt;

        private Builder() {
        }

        public Builder planMealId(PlanMealId val) {
            planMealId = val;
            return this;
        }

        public Builder planId(PlanId val) {
            planId = val;
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

        public Builder pricePerMonth(Money val) {
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

        public Builder imageUrl(String val) {
            imageUrl = val;
            return this;
        }

        public Builder createdAt(LocalDateTime val) {
            createdAt = val;
            return this;
        }

        public Builder updatedAt(LocalDateTime val) {
            updatedAt = val;
            return this;
        }

        public Builder deleteFlag(boolean val) {
            deleteFlag = val;
            return this;
        }

        public Builder deletedAt(LocalDateTime val) {
            deletedAt = val;
            return this;
        }

        public PlanMeal build() {
            return new PlanMeal(this);
        }
    }
}
