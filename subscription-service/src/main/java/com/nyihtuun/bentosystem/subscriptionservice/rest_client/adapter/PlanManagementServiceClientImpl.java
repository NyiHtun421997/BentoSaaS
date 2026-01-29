package com.nyihtuun.bentosystem.subscriptionservice.rest_client.adapter;

import com.nyihtuun.bentosystem.domain.dto.response.PlanMealResponseDto;
import com.nyihtuun.bentosystem.domain.dto.response.PlanResponseDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionRequestDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client.PlanManagementServiceClient;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client.PlanData;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client.PlanValidationResult;
import com.nyihtuun.bentosystem.subscriptionservice.configuration.SubscriptionConfigData;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class PlanManagementServiceClientImpl implements PlanManagementServiceClient {

    private final RestClient restClient;
    private final SubscriptionConfigData configData;

    @Autowired
    public PlanManagementServiceClientImpl(RestClient restClient, SubscriptionConfigData configData) {
        this.restClient = restClient;
        this.configData = configData;
    }

    @Override
    @Retry(name = "plan-management-service", fallbackMethod = "fallbackValidateAndFetchExistingPlanAndPlanMeals")
    @CircuitBreaker(name = "plan-management-service", fallbackMethod = "fallbackValidateAndFetchExistingPlanAndPlanMeals")
    public PlanValidationResult<PlanData> validateAndFetchExistingPlanAndPlanMeals(SubscriptionRequestDto subscriptionRequestDto) {
        log.info("Validating plan with id: {}.", subscriptionRequestDto.getPlanId());
        log.info("Fetching plan meals for plan with id: {}.", subscriptionRequestDto.getPlanId());

        PlanResponseDto planResponseDto = restClient.get()
                                                    .uri(uriBuilder -> uriBuilder.pathSegment(
                                                                                         configData.planManagementApi(),
                                                                                         configData.planManagementVersion(),
                                                                                         configData.planManagementPlanDetailsPath(),
                                                                                         subscriptionRequestDto.getPlanId().toString())
                                                                                 .build())
                                                    .retrieve()
                                                    .body(PlanResponseDto.class);

        if (planResponseDto == null || planResponseDto.getPlanMealResponseDtos() == null || planResponseDto.getPlanMealResponseDtos()
                                                                                                           .isEmpty()) {
            log.error("Plan with id: {} is invalid.", subscriptionRequestDto.getPlanId());
            return new PlanValidationResult<>(PlanValidationResult.PlanValidationStatus.INVALID_PLAN,
                                              null);
        }

        log.info("Plan with id: {} is valid and contains the following plan meals: {}.", planResponseDto.getPlanId(), planResponseDto.getPlanMealResponseDtos());
        List<UUID> existingPlanMealIds = planResponseDto.getPlanMealResponseDtos()
                                         .stream()
                                         .map(PlanMealResponseDto::getPlanMealId)
                                         .toList();

        return new PlanValidationResult<>(PlanValidationResult.PlanValidationStatus.VALID_PLAN,
                                          new PlanData(planResponseDto.getPlanId(), existingPlanMealIds));
    }

    PlanValidationResult<PlanData> fallbackValidateAndFetchExistingPlanAndPlanMeals(SubscriptionRequestDto subscriptionRequestDto,
                                                                                               Throwable e) {
        log.error("Plan validation failed for planId={} and resolved to fallback.", subscriptionRequestDto.getPlanId(), e);
        return new PlanValidationResult<>(PlanValidationResult.PlanValidationStatus.API_FAILURE, null);
    }
}
