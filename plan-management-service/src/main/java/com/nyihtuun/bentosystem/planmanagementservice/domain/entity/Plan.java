package com.nyihtuun.bentosystem.planmanagementservice.domain.entity;

import com.nyihtuun.bentosystem.domain.entity.AggregateRoot;
import com.nyihtuun.bentosystem.planmanagementservice.domain.status.PlanStatus;
import com.nyihtuun.bentosystem.domain.valueobject.*;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
public class Plan extends AggregateRoot<PlanId> {
    private Code code;
    private String title;
    private String description;
    private PlanStatus status;
    private CategoryId categoryId;
    private UserId providerUserId;
    private Address address;
    private Money displaySubscriptionFee;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    private final List<PlanMeal> planMeals = new ArrayList<>();

    private Plan(Builder builder) {
        super.setId(builder.planId);
        code = builder.code;
        title = builder.title;
        description = builder.description;
        status = builder.status;
        createdAt = builder.createdAt;
        updatedAt = builder.updatedAt;
        categoryId = builder.categoryId;
        providerUserId = builder.providerUserId;
        address = builder.address;
        displaySubscriptionFee = builder.displaySubscriptionFee;
    }

    public static final class Builder {
        private PlanId planId;
        private Code code;
        private String title;
        private String description;
        private PlanStatus status;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;
        private CategoryId categoryId;
        private UserId providerUserId;
        private Address address;
        private Money displaySubscriptionFee;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder planId(PlanId val) {
            planId = val;
            return this;
        }

        public Builder code(Code val) {
            code = val;
            return this;
        }

        public Builder title(String val) {
            title = val;
            return this;
        }

        public Builder description(String val) {
            description = val;
            return this;
        }

        public Builder status(PlanStatus val) {
            status = val;
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

        public Builder categoryId(CategoryId val) {
            categoryId = val;
            return this;
        }

        public Builder providerUserId(UserId val) {
            providerUserId = val;
            return this;
        }

        public Builder address(Address val) {
            address = val;
            return this;
        }

        public Builder displaySubscriptionFee(Money val) {
            displaySubscriptionFee = val;
            return this;
        }

        public Plan build() {
            return new Plan(this);
        }
    }
}
