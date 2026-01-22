package com.nyihtuun.bentosystem.subscriptionservice.domain.entity;

import com.nyihtuun.bentosystem.domain.entity.AggregateRoot;
import com.nyihtuun.bentosystem.domain.valueobject.*;
import com.nyihtuun.bentosystem.domain.valueobject.status.SubscriptionStatus;
import com.nyihtuun.bentosystem.subscriptionservice.domain.exception.SubscriptionDomainException;
import com.nyihtuun.bentosystem.subscriptionservice.domain.exception.SubscriptionErrorCode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class Subscription extends AggregateRoot<SubscriptionId> {
    private UserId userId;
    private final PlanId planId;
    private final UserId providedUserId;
    private SubscriptionStatus subscriptionStatus;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime activatedAt;

    private List<MealSelection> mealSelections;

    private Subscription(Builder builder) {
        super.setId(builder.subscriptionId);
        userId = builder.userId;
        planId = builder.planId;
        providedUserId = builder.providedUserId;
        subscriptionStatus = builder.subscriptionStatus;
        appliedAt = builder.appliedAt;
        updatedAt = builder.updatedAt;
        cancelledAt = builder.cancelledAt;
        activatedAt = builder.activatedAt;
        mealSelections = builder.mealSelections;
    }

    public void validateSubscription() {
        if (this.getMealSelections() == null || this.getMealSelections().isEmpty())
            throw new SubscriptionDomainException(SubscriptionErrorCode.EMPTY_MEALS);
    }

    public void initializeSubscription(UserId userId) {
        super.setId(new SubscriptionId(UUID.randomUUID()));
        this.userId = userId;
        this.subscriptionStatus = SubscriptionStatus.APPLIED;
        this.appliedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        this.mealSelections.forEach(mealSelection -> mealSelection.initializeMealSelection(super.getId()));
    }

    public void activate() {
        this.subscriptionStatus = SubscriptionStatus.SUBSCRIBED;
    }

    public void suspend() {
        this.subscriptionStatus = SubscriptionStatus.SUSPENDED;
    }

    public void cancel() {
        this.subscriptionStatus = SubscriptionStatus.CANCELLED;
    }

    public void updateMealSelections(List<UUID> planMealIds) {
        Set<UUID> desired = new HashSet<>(planMealIds);
        this.mealSelections.removeIf(mealSelection -> {
           boolean contains = desired.contains(mealSelection.getId().getPlanMealId().getValue());
           if (contains) desired.remove(mealSelection.getId().getPlanMealId().getValue());
           return !contains;
        });

        for (UUID planMealId : desired) {
            this.mealSelections.add(MealSelection.builder()
                                            .subscriptionId(super.getId())
                                            .planMealId(new PlanMealId(planMealId))
                                            .build());
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private SubscriptionId subscriptionId;
        private UserId userId;
        private PlanId planId;
        private UserId providedUserId;
        private SubscriptionStatus subscriptionStatus;
        private LocalDateTime appliedAt;
        private LocalDateTime updatedAt;
        private LocalDateTime cancelledAt;
        private LocalDateTime activatedAt;
        private List<MealSelection> mealSelections;

        private Builder() {
        }

        public Builder subscriptionId(SubscriptionId val) {
            subscriptionId = val;
            return this;
        }

        public Builder userId(UserId val) {
            userId = val;
            return this;
        }

        public Builder planId(PlanId val) {
            planId = val;
            return this;
        }

        public Builder providedUserId(UserId val) {
            providedUserId = val;
            return this;
        }

        public Builder subscriptionStatus(SubscriptionStatus val) {
            subscriptionStatus = val;
            return this;
        }

        public Builder appliedAt(LocalDateTime val) {
            appliedAt = val;
            return this;
        }

        public Builder updatedAt(LocalDateTime val) {
            updatedAt = val;
            return this;
        }

        public Builder cancelledAt(LocalDateTime val) {
            cancelledAt = val;
            return this;
        }

        public Builder activatedAt(LocalDateTime val) {
            activatedAt = val;
            return this;
        }

        public Builder mealSelections(List<MealSelection> val) {
            mealSelections = val;
            return this;
        }

        public Subscription build() {
            return new Subscription(this);
        }
    }
}
