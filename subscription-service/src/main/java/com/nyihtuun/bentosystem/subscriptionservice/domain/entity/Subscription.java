package com.nyihtuun.bentosystem.subscriptionservice.domain.entity;

import com.nyihtuun.bentosystem.domain.entity.AggregateRoot;
import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.SubscriptionId;
import com.nyihtuun.bentosystem.domain.valueobject.UserId;
import com.nyihtuun.bentosystem.domain.valueobject.status.SubscriptionStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class Subscription extends AggregateRoot<SubscriptionId> {
    private final UserId userId;
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
