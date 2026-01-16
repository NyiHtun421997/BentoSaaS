package com.nyihtuun.bentosystem.planmanagementservice.domain.entity;

import com.nyihtuun.bentosystem.domain.entity.AggregateRoot;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementDomainException;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementErrorCode;
import com.nyihtuun.bentosystem.domain.valueobject.status.PlanStatus;
import com.nyihtuun.bentosystem.domain.valueobject.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Getter
public class Plan extends AggregateRoot<PlanId> {
    private static final int MAX_SKIPDAYS = 2;

    private Code code;
    private String title;
    private String description;
    private PlanStatus status;
    private Set<CategoryId> categoryIds;
    private UserId providerUserId;
    private List<LocalDate> skipDays;
    private Address address;
    private Money displaySubscriptionFee;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<PlanMeal> planMeals = new ArrayList<>();
    private boolean deleteFlag;
    private LocalDateTime deletedAt;

    private Plan(Builder builder) {
        super.setId(builder.planId);
        code = builder.code;
        title = builder.title;
        description = builder.description;
        status = builder.status;
        categoryIds = builder.categoryIds;
        providerUserId = builder.providerUserId;
        skipDays = builder.skipDays;
        address = builder.address;
        displaySubscriptionFee = builder.displaySubscriptionFee;
        createdAt = builder.createdAt;
        updatedAt = builder.updatedAt;
        planMeals = builder.planMeals;
        deleteFlag = builder.deleteFlag;
        deletedAt = builder.deletedAt;
    }

    private void checkEmptyPlanMeals() {
        if (this.planMeals.isEmpty())
            throw new PlanManagementDomainException(PlanManagementErrorCode.EMPTY_MEALS);
    }

    private void checkAtLeasOnePrimaryMeal() {
        if (this.planMeals.stream().noneMatch(PlanMeal::isPrimary))
            throw new PlanManagementDomainException(PlanManagementErrorCode.NO_PRIMARY_MEAL);
    }

    private void validateSubscriptionFee() {
        Money totalPlanMealsPrice = this.planMeals.stream()
                                     .map(PlanMeal::getPricePerMonth)
                                     .reduce(new Money(BigDecimal.ZERO),
                                             Money::add);
        if (!totalPlanMealsPrice.equals(this.displaySubscriptionFee))
            throw new PlanManagementDomainException(PlanManagementErrorCode.INVALID_SUB_FEE);
    }

    private void uniquifyAndValidateSkipDays() {
        this.skipDays = new ArrayList<>(new HashSet<>(this.skipDays));
        if (this.skipDays.size() > MAX_SKIPDAYS)
            throw new PlanManagementDomainException(PlanManagementErrorCode.INVALID_SKIPDAYS);
    }

    public void validatePlan() {
        checkEmptyPlanMeals();
        checkAtLeasOnePrimaryMeal();
        this.planMeals.forEach(PlanMeal::validateMeal);
        validateSubscriptionFee();
        uniquifyAndValidateSkipDays();
    }

    public void initializePlan() {
        super.setId(new PlanId(UUID.randomUUID()));
        this.code = Code.generate();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.planMeals.forEach(planMeal -> planMeal.initializeMeal(super.getId()));
        status = PlanStatus.RECRUITING;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private PlanId planId;
        private Code code;
        private String title;
        private String description;
        private PlanStatus status;
        private Set<CategoryId> categoryIds;
        private UserId providerUserId;
        private List<LocalDate> skipDays;
        private Address address;
        private Money displaySubscriptionFee;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<PlanMeal> planMeals;
        private boolean deleteFlag;
        private LocalDateTime deletedAt;

        private Builder() {
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

        public Builder categoryIds(Set<CategoryId> val) {
            categoryIds = val;
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

        public Builder createdAt(LocalDateTime val) {
            createdAt = val;
            return this;
        }

        public Builder updatedAt(LocalDateTime val) {
            updatedAt = val;
            return this;
        }

        public Builder planMeals(List<PlanMeal> val) {
            planMeals = val;
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

        public Builder skipDays(List<LocalDate> val) {
            skipDays = val;
            return this;
        }

        public Plan build() {
            return new Plan(this);
        }
    }
}
