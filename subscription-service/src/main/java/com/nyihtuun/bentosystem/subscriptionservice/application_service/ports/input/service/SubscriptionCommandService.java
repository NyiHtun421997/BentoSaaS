package com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.service;

import com.nyihtuun.bentosystem.domain.valueobject.SubscriptionId;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionRequestDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionResponseDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client.PlanManagementServiceClient;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client.PlanValidationResult;
import com.nyihtuun.bentosystem.subscriptionservice.domain.exception.SubscriptionDomainException;
import com.nyihtuun.bentosystem.subscriptionservice.domain.exception.SubscriptionErrorCode;

public interface SubscriptionCommandService {
    SubscriptionResponseDto validateAndInitiateSubscription(SubscriptionRequestDto subscriptionRequestDto);

    SubscriptionResponseDto validateAndUpdateSubscription(SubscriptionId subscriptionId, SubscriptionRequestDto subscriptionRequestDto);

    SubscriptionResponseDto cancelSubscription(SubscriptionId subscriptionId);

    SubscriptionResponseDto reflectPlanChanged();

    default SubscriptionRequestDto validatePlanAndPlanMeals(SubscriptionRequestDto subscriptionRequestDto) {
        PlanValidationResult<SubscriptionRequestDto> validationResult = getPlanManagementServiceClient().validateAndFetchLegitPlanAndPlanMeals(
                subscriptionRequestDto);

        return switch (validationResult.getPlanValidationStatus()) {
            case VALID_PLAN -> validationResult.getDto();
            case INVALID_PLAN -> throw new SubscriptionDomainException(SubscriptionErrorCode.INVALID_PLAN);
            case API_FAILURE -> throw new SubscriptionDomainException(SubscriptionErrorCode.VALIDATION_FAILURE);
        };
    }

    PlanManagementServiceClient getPlanManagementServiceClient();
}
