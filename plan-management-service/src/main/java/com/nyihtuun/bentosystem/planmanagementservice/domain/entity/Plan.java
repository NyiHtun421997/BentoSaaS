package com.nyihtuun.bentosystem.planmanagementservice.domain.entity;

import com.nyihtuun.bentosystem.domain.entity.AggregateRoot;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementDomainException;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementErrorCode;
import com.nyihtuun.bentosystem.domain.valueobject.status.PlanStatus;
import com.nyihtuun.bentosystem.domain.valueobject.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
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

    private List<PlanMeal> planMeals;
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
        Money totalPlanMealsPrice = calculateDisplaySubscriptionFee();
        if (!totalPlanMealsPrice.equals(this.displaySubscriptionFee) || !this.displaySubscriptionFee.isGreaterThanZero())
            throw new PlanManagementDomainException(PlanManagementErrorCode.INVALID_SUB_FEE);
    }

    private Money calculateDisplaySubscriptionFee() {
        return this.planMeals.stream()
                             .filter(planMeal -> !planMeal.isDeleteFlag())
                             .map(PlanMeal::getPricePerMonth)
                             .reduce(new Money(BigDecimal.ZERO), Money::add);
    }

    private void uniquifyAndValidateSkipDays() {
        this.skipDays = new ArrayList<>(new HashSet<>(this.skipDays));
        if (this.skipDays.size() > MAX_SKIPDAYS)
            throw new PlanManagementDomainException(PlanManagementErrorCode.INVALID_SKIPDAYS);
    }

    public void validatePlan() {
        checkEmptyPlanMeals();
        validateSubscriptionFee();
        checkAtLeasOnePrimaryMeal();
        this.planMeals.forEach(PlanMeal::validateMeal);
        validateSubscriptionFee();
        uniquifyAndValidateSkipDays();
    }

    public void initializePlan(UserId userId) {
        super.setId(new PlanId(UUID.randomUUID()));
        this.code = Code.generate();
        this.providerUserId = userId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.planMeals.forEach(planMeal -> planMeal.initializeMeal(super.getId()));
        status = PlanStatus.RECRUITING;
    }

    public void updatePlan(PlanUpdateCommand planUpdateCommand) {
        this.title = planUpdateCommand.getTitle();
        this.description = planUpdateCommand.getDescription();
        this.categoryIds = planUpdateCommand.getCategoryIds();
        this.skipDays = planUpdateCommand.getSkipDays();
        this.address = planUpdateCommand.getAddress();
        this.displaySubscriptionFee = planUpdateCommand.getDisplaySubscriptionFee();
        this.updatedAt = LocalDateTime.now();
    }

    public void deletePlan() {
        this.deleteFlag = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void reflectMealSelection(List<PlanMealId> appliedPlanMealIds,
                                     List<PlanMealId> unappliedPlanMealIds) {

        Set<PlanMealId> applied = new HashSet<>(appliedPlanMealIds);
        Set<PlanMealId> unapplied = new HashSet<>(unappliedPlanMealIds);

        for (PlanMeal meal : this.planMeals) {
            PlanMealId id = meal.getId();

            if (applied.contains(id)) {
                meal.reflectUserSelection(true);
            } else if (unapplied.contains(id)) {
                meal.reflectUserSelection(false);
            }
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePlanStatus() {
        boolean allMatched = this.planMeals.stream()
                                           .filter(PlanMeal::isPrimary)
                                           .allMatch(planMeal -> planMeal.getMinSubCount().isMetBy(planMeal.getCurrentSubCount()));
        if (allMatched) {
            this.status = PlanStatus.ACTIVE;
            return;
        }
        if (!this.status.equals(PlanStatus.RECRUITING))
            this.status = PlanStatus.SUSPENDED;
    }

    public void addMeal(PlanMeal planMeal) {
        planMeal.validateMeal();
        planMeal.initializeMeal(super.getId());
        this.planMeals.add(planMeal);
        this.displaySubscriptionFee = this.getDisplaySubscriptionFee().add(planMeal.getPricePerMonth());
        this.updatedAt = LocalDateTime.now();

        validateSubscriptionFee();
    }

    public void removeMeal(PlanMealId planMealId) {
        PlanMeal planMealToRemove = this.getPlanMeals().stream()
                                        .filter(planMeal -> planMeal.getId().equals(planMealId))
                                        .findFirst()
                                        .orElseThrow(() -> new PlanManagementDomainException(PlanManagementErrorCode.INVALID_PLANMEAL_ID));

        long primaryMealCount = this.planMeals.stream()
                                              .filter(PlanMeal::isPrimary)
                                              .count();

        if (primaryMealCount == 1 && planMealToRemove.isPrimary())
            throw new PlanManagementDomainException(PlanManagementErrorCode.NO_PRIMARY_MEAL);

        planMealToRemove.deleteMeal();
        this.displaySubscriptionFee = this.getDisplaySubscriptionFee().subtract(planMealToRemove.getPricePerMonth());
        this.updatedAt = LocalDateTime.now();

        validateSubscriptionFee();
        checkAtLeasOnePrimaryMeal();
    }

    public void updateMeal(PlanMealId planMealId, PlanMealUpdateCommand planMealUpdateCommand) {
        PlanMeal planMealToUpdate = this.getPlanMeals().stream()
                                        .filter(planMeal -> planMeal.getId().equals(planMealId))
                                        .findFirst()
                                        .orElseThrow(() -> new PlanManagementDomainException(PlanManagementErrorCode.INVALID_PLANMEAL_ID));
        planMealToUpdate.updateMeal(planMealUpdateCommand);
        planMealToUpdate.validateMeal();
        this.displaySubscriptionFee = calculateDisplaySubscriptionFee();
        this.updatedAt = LocalDateTime.now();

        validateSubscriptionFee();
        checkAtLeasOnePrimaryMeal();
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
