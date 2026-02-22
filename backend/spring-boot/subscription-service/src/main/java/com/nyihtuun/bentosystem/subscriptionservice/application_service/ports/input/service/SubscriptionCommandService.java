package com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.service;

import com.google.protobuf.Message;
import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import com.nyihtuun.bentosystem.domain.valueobject.SubscriptionId;
import com.nyihtuun.bentosystem.domain.valueobject.UserId;
import com.nyihtuun.bentosystem.domain.valueobject.status.PlanStatus;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionRequestDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionResponseDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client.PlanManagementServiceClient;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client.PlanData;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client.PlanValidationResult;
import com.nyihtuun.bentosystem.subscriptionservice.domain.entity.Subscription;
import com.nyihtuun.bentosystem.subscriptionservice.domain.exception.SubscriptionDomainException;
import com.nyihtuun.bentosystem.subscriptionservice.domain.exception.SubscriptionErrorCode;
import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public interface SubscriptionCommandService {
    SubscriptionResponseDto validateAndInitiateSubscription(SubscriptionRequestDto subscriptionRequestDto, UserId userId);

    SubscriptionResponseDto validateAndUpdateSubscription(SubscriptionId subscriptionId, SubscriptionRequestDto subscriptionRequestDto);

    SubscriptionResponseDto cancelSubscription(SubscriptionId subscriptionId);

    List<SubscriptionResponseDto> reflectPlanChanged(PlanId planId, PlanStatus planStatus);

    List<SubscriptionResponseDto> reflectPlanMealsRemoved(PlanId planId, List<PlanMealId> planMealIds);

    void createOutboxMessageAndPersist(SubscriptionResponseDto subscription, Supplier<Message> eventSupplier, String outboxMsgType, String topicName);

    default PlanData validatePlanAndPlanMeals(SubscriptionRequestDto subscriptionRequestDto) {
        PlanValidationResult<PlanData> validationResult = getPlanManagementServiceClient().validateAndFetchExistingPlanAndPlanMeals(
                subscriptionRequestDto);

        return switch (validationResult.getPlanValidationStatus()) {
            case VALID_PLAN -> {
                PlanData resultData = validationResult.getData();

                Set<UUID> existingPlanMealIds = new HashSet<>(resultData.planMealIds());

                List<@NotNull UUID> legitPlanMealIds = subscriptionRequestDto.getPlanMealIds()
                                                                             .stream()
                                                                             .filter(existingPlanMealIds::contains)
                                                                             .toList();

                yield new PlanData(resultData.planId(), legitPlanMealIds);
            }
            case INVALID_PLAN -> throw new SubscriptionDomainException(SubscriptionErrorCode.INVALID_PLAN);
            case API_FAILURE -> throw new SubscriptionDomainException(SubscriptionErrorCode.VALIDATION_FAILURE);
        };
    }

    PlanManagementServiceClient getPlanManagementServiceClient();
}
