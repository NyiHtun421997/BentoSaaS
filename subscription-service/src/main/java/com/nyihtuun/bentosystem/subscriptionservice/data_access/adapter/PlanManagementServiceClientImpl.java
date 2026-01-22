package com.nyihtuun.bentosystem.subscriptionservice.data_access.adapter;

import com.nyihtuun.bentosystem.domain.dto.response.PlanMealResponseDto;
import com.nyihtuun.bentosystem.domain.dto.response.PlanResponseDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionRequestDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client.PlanManagementServiceClient;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client.PlanValidationResult;
import com.nyihtuun.bentosystem.subscriptionservice.configuration.SubscriptionConfigData;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    @Retry(name = "plan-management-service", fallbackMethod = "fallbackValidateAndFetchLegitPlanAndPlanMeals")
    @CircuitBreaker(name = "plan-management-service", fallbackMethod = "fallbackValidateAndFetchLegitPlanAndPlanMeals")
    public PlanValidationResult<SubscriptionRequestDto> validateAndFetchLegitPlanAndPlanMeals(SubscriptionRequestDto subscriptionRequestDto) {
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

        Set<UUID> existingPlanMealIds = planResponseDto.getPlanMealResponseDtos().stream()
                                                       .map(PlanMealResponseDto::getPlanMealId)
                                                       .collect(Collectors.toSet());

        List<@NotNull UUID> legitPlanMealIds = subscriptionRequestDto.getPlanMealIds()
                                                                     .stream()
                                                                     .filter(existingPlanMealIds::contains)
                                                                     .toList();

        log.info("Plan with id: {} is valid and contains legit plan meals: {}.", subscriptionRequestDto.getPlanId(), legitPlanMealIds);
        return new PlanValidationResult<>(PlanValidationResult.PlanValidationStatus.VALID_PLAN,
                                          SubscriptionRequestDto.builder()
                                                                .planId(planResponseDto.getPlanId())
                                                                .planMealIds(legitPlanMealIds)
                                                                .build());
    }

    PlanValidationResult<SubscriptionRequestDto> fallbackValidateAndFetchLegitPlanAndPlanMeals(SubscriptionRequestDto subscriptionRequestDto, Throwable e) {
        log.error("Plan validation failed for plan with id: {} and resolved to fallback.", subscriptionRequestDto.getPlanId(), e);
        return new PlanValidationResult<>(PlanValidationResult.PlanValidationStatus.API_FAILURE, null);
    }
}
