package com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.service;

import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SubscriptionQueryService {
    List<SubscriptionResponseDto> getMySubscriptions(UUID userId, LocalDate since);
    SubscriptionResponseDto getSubscriptionById(UUID subscriptionId);
}
