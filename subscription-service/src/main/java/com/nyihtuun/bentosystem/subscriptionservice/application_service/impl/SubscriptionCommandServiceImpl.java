package com.nyihtuun.bentosystem.subscriptionservice.application_service.impl;

import com.nyihtuun.bentosystem.domain.valueobject.SubscriptionId;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionRequestDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionResponseDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.mapper.SubscriptionDataMapper;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.service.SubscriptionCommandService;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client.PlanManagementServiceClient;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.repository.SubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SubscriptionCommandServiceImpl implements SubscriptionCommandService {

    private final SubscriptionDataMapper subscriptionDataMapper;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanManagementServiceClient planManagementServiceClient;

    @Autowired
    public SubscriptionCommandServiceImpl(SubscriptionDataMapper subscriptionDataMapper, SubscriptionRepository subscriptionRepository,
                                          PlanManagementServiceClient planManagementServiceClient) {
        this.subscriptionDataMapper = subscriptionDataMapper;
        this.subscriptionRepository = subscriptionRepository;
        this.planManagementServiceClient = planManagementServiceClient;
    }

    @Override
    public SubscriptionResponseDto validateAndInitiateSubscription(SubscriptionRequestDto subscriptionRequestDto) {
        return null;
    }

    @Override
    public SubscriptionResponseDto validateAndUpdateSubscription(SubscriptionId subscriptionId,
                                                                 SubscriptionRequestDto subscriptionRequestDto) {
        return null;
    }

    @Override
    public SubscriptionResponseDto cancelSubscription(SubscriptionId subscriptionId) {
        return null;
    }


    @Override
    public PlanManagementServiceClient getPlanManagementServiceClient() {
        return planManagementServiceClient;
    }

    @Override
    public SubscriptionResponseDto reflectPlanChanged() {
        return null;
    }
}
