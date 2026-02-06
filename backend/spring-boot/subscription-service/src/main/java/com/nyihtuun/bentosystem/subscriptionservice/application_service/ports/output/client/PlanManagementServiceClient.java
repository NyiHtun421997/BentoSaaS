package com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client;

import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionRequestDto;

public interface PlanManagementServiceClient {
    PlanValidationResult<PlanData> validateAndFetchExistingPlanAndPlanMeals(SubscriptionRequestDto subscriptionRequestDto);
}
