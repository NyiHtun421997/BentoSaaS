package com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.service;

import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionResponseDto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionQueryService {
    List<SubscriptionResponseDto> getMySubscriptions(UUID userId, LocalDate since);
    Optional<SubscriptionResponseDto> getSubscriptionById(UUID subscriptionId);
    List<SubscriptionResponseDto> getActiveSubscriptionsBefore(Instant before);
    List<SubscriptionResponseDto> getSubscriptionsByPlanId(UUID planId);
}
