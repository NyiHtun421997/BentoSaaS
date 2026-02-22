package com.nyihtuun.bentosystem.subscriptionservice.application_service.impl.service;

import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionResponseDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.mapper.SubscriptionDataMapper;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.service.SubscriptionQueryService;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.repository.SubscriptionRepository;
import com.nyihtuun.bentosystem.subscriptionservice.security.authorization_handler.SubscriptionServiceAccessDeniedHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.method.HandleAuthorizationDenied;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
    @Transactional(readOnly = true)
    @PreAuthorize("principal.toString() == #userId.toString()")
    @HandleAuthorizationDenied(handlerClass = SubscriptionServiceAccessDeniedHandler.class)
    public List<SubscriptionResponseDto> getMySubscriptions(UUID userId, LocalDate since) {
        return subscriptionRepository.findAllSubscriptionsByUserIdAndDate(userId, since)
                                     .stream()
                                     .map(subscriptionDataMapper::mapSubscriptionToSubscriptionDto)
                                     .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PostAuthorize("returnObject.isPresent() ? returnObject.get().userId.toString() == principal.toString() : true")
    @HandleAuthorizationDenied(handlerClass = SubscriptionServiceAccessDeniedHandler.class)
    public Optional<SubscriptionResponseDto> getSubscriptionById(UUID subscriptionId) {
        return subscriptionRepository.findBySubscriptionId(subscriptionId)
                                     .map(subscriptionDataMapper::mapSubscriptionToSubscriptionDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponseDto> getActiveSubscriptionsBefore(Instant before) {
        return subscriptionRepository.findActiveSubscriptionsBeforeDate(before)
                                     .stream()
                                     .map(subscriptionDataMapper::mapSubscriptionToSubscriptionDto)
                                     .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponseDto> getSubscriptionsByPlanId(UUID planId) {
        return subscriptionRepository.findByPlanId(planId)
                                     .stream()
                                     .map(subscriptionDataMapper::mapSubscriptionToSubscriptionDto)
                                     .toList();
    }
}
