package com.nyihtuun.bentosystem.subscriptionservice.application_service.impl;

import com.nyihtuun.bentosystem.domain.valueobject.SubscriptionId;
import com.nyihtuun.bentosystem.domain.valueobject.UserId;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionRequestDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionResponseDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.mapper.SubscriptionDataMapper;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.service.SubscriptionCommandService;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client.PlanData;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client.PlanManagementServiceClient;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.repository.SubscriptionRepository;
import com.nyihtuun.bentosystem.subscriptionservice.domain.entity.Subscription;
import com.nyihtuun.bentosystem.subscriptionservice.domain.exception.SubscriptionDomainException;
import com.nyihtuun.bentosystem.subscriptionservice.domain.exception.SubscriptionErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public SubscriptionResponseDto validateAndInitiateSubscription(SubscriptionRequestDto subscriptionRequestDto, UserId userId) {
        log.info("Validating and initiating subscription: {}", subscriptionRequestDto);

        PlanData validPlanData = validatePlanAndPlanMeals(subscriptionRequestDto);
        Subscription subscription = subscriptionDataMapper.mapToSubscription(validPlanData,
                                                                             new UserId(subscriptionRequestDto.getProvidedUserId()));
        subscription.validateSubscription();
        subscription.initializeSubscription(userId);

        log.info("Subscription with id: {} is validated and initiated", subscription.getId().getValue());

        try {
            Subscription savedSubscription = persist(subscription, true);
            return subscriptionDataMapper.mapSubscriptionToSubscriptionDto(savedSubscription);
        } catch (DataIntegrityViolationException e) {
            log.error("Subscription with id: {} already exists", subscription.getId().getValue());
            throw new SubscriptionDomainException(SubscriptionErrorCode.SUBSCRIPTION_ALREADY_EXISTS);
        }
    }

    @Override
    public SubscriptionResponseDto validateAndUpdateSubscription(SubscriptionId subscriptionId,
                                                                 SubscriptionRequestDto subscriptionRequestDto) {
        log.info("Validating and updating subscription with id: {} with new data: {}.", subscriptionId, subscriptionRequestDto);
        Subscription subscription = subscriptionRepository.findBySubscriptionId(subscriptionId.getValue())
                                                          .orElseThrow(() -> new SubscriptionDomainException(SubscriptionErrorCode.INVALID_SUBSCRIPTION_ID));
        PlanData validPlanData = validatePlanAndPlanMeals(subscriptionRequestDto);

        subscription.updateMealSelections(validPlanData.planMealIds());
        log.info("Subscription with id: {} is updated", subscriptionId.getValue());

        Subscription savedSubscription = persist(subscription, false);
        return subscriptionDataMapper.mapSubscriptionToSubscriptionDto(savedSubscription);
    }

    @Override
    public SubscriptionResponseDto cancelSubscription(SubscriptionId subscriptionId) {
        log.info("Cancelling subscription with id: {}", subscriptionId);
        Subscription subscription = subscriptionRepository.findBySubscriptionId(subscriptionId.getValue())
                                                          .orElseThrow(() -> new SubscriptionDomainException(SubscriptionErrorCode.INVALID_SUBSCRIPTION_ID));
        subscription.cancel();
        log.info("Subscription with id: {} is cancelled", subscriptionId.getValue());

        Subscription savedSubscription = persist(subscription, false);
        return subscriptionDataMapper.mapSubscriptionToSubscriptionDto(savedSubscription);
    }

    private Subscription persist(Subscription subscription, boolean flush) {
        Subscription savedSubscription = subscriptionRepository.save(subscription, flush);
        log.info("Subscription with id: {} is persisted", subscription.getId().getValue());
        return savedSubscription;
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
