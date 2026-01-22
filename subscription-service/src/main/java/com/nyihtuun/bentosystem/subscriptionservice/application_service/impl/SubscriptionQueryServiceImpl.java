package com.nyihtuun.bentosystem.subscriptionservice.application_service.impl;

import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionResponseDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.mapper.SubscriptionDataMapper;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.service.SubscriptionQueryService;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.repository.SubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class SubscriptionQueryServiceImpl implements SubscriptionQueryService {

    private final SubscriptionDataMapper subscriptionDataMapper;
    private final SubscriptionRepository subscriptionRepository;

    @Autowired
    public SubscriptionQueryServiceImpl(SubscriptionDataMapper subscriptionDataMapper, SubscriptionRepository subscriptionRepository) {
        this.subscriptionDataMapper = subscriptionDataMapper;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public List<SubscriptionResponseDto> getMySubscriptions(UUID userId, LocalDate since) {
        return List.of();
    }

    @Override
    public SubscriptionResponseDto getSubscriptionById(UUID subscriptionId) {
        return null;
    }
}
