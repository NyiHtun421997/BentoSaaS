package com.nyihtuun.bentosystem.subscriptionservice.application_service.impl.service;

import com.google.protobuf.Message;
import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import com.nyihtuun.bentosystem.domain.valueobject.SubscriptionId;
import com.nyihtuun.bentosystem.domain.valueobject.UserId;
import com.nyihtuun.bentosystem.domain.valueobject.status.OutboxStatus;
import com.nyihtuun.bentosystem.domain.valueobject.status.PlanStatus;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionRequestDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionResponseDto;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.mapper.SubscriptionDataMapper;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.outbox.model.UserPlanSubscriptionEventOutboxMessage;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.service.SubscriptionCommandService;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client.PlanData;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client.PlanManagementServiceClient;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.repository.SubscriptionRepository;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.repository.UserPlanSubscriptionEventOutboxRepository;
import com.nyihtuun.bentosystem.subscriptionservice.configuration.SubscriptionConfigData;
import com.nyihtuun.bentosystem.subscriptionservice.domain.entity.Subscription;
import com.nyihtuun.bentosystem.subscriptionservice.domain.exception.SubscriptionDomainException;
import com.nyihtuun.bentosystem.subscriptionservice.domain.exception.SubscriptionErrorCode;
import com.nyihtuun.bentosystem.subscriptionservice.security.authorization_handler.SubscriptionServiceAccessDeniedHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.authorization.method.HandleAuthorizationDenied;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.nyihtuun.bentosystem.domain.utility.CommonConstants.DATA_CHANGED;
import static com.nyihtuun.bentosystem.subscriptionservice.application_service.impl.service.PlanEventHelper.USER_PLAN_SUBSCRIPTION_EVENT_CREATION_FUNCTION;

@Slf4j
@AllArgsConstructor
@Service
public class SubscriptionCommandServiceImpl implements SubscriptionCommandService {

    private final SubscriptionDataMapper subscriptionDataMapper;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanManagementServiceClient planManagementServiceClient;
    private final UserPlanSubscriptionEventOutboxRepository userPlanSubscriptionEventOutboxRepository;
    private final SubscriptionConfigData subscriptionConfigData;

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
            SubscriptionResponseDto subscriptionResponseDto = subscriptionDataMapper.mapSubscriptionToSubscriptionDto(savedSubscription);

            Supplier<Message> eventCreationSupplier =
                    () -> USER_PLAN_SUBSCRIPTION_EVENT_CREATION_FUNCTION.execute(subscriptionResponseDto,
                                                                                 validPlanData.planMealIds(),
                                                                                 Collections.emptyList());
            createOutboxMessageAndPersist(subscriptionResponseDto, eventCreationSupplier, DATA_CHANGED, subscriptionConfigData.userSubscriptionTopicName());

            return subscriptionResponseDto;
        } catch (DataIntegrityViolationException e) {
            log.error("Subscription with id: {} already exists", subscription.getId().getValue());
            throw new SubscriptionDomainException(SubscriptionErrorCode.SUBSCRIPTION_ALREADY_EXISTS);
        }
    }

    @Override
    @Transactional
    @PostAuthorize("returnObject.userId.toString() == principal.toString()")
    @HandleAuthorizationDenied(handlerClass = SubscriptionServiceAccessDeniedHandler.class)
    public SubscriptionResponseDto validateAndUpdateSubscription(SubscriptionId subscriptionId,
                                                                 SubscriptionRequestDto subscriptionRequestDto) {
        log.info("Validating and updating subscription with id: {} with new data: {}.", subscriptionId, subscriptionRequestDto);
        Subscription subscription = subscriptionRepository.findBySubscriptionId(subscriptionId.getValue())
                                                          .orElseThrow(() -> new SubscriptionDomainException(SubscriptionErrorCode.INVALID_SUBSCRIPTION_ID));

        Set<UUID> planMealIdsBefore = subscription.getMealSelections().stream()
                                                  .map(mealSelection -> mealSelection.getId().getPlanMealId().getValue())
                                                  .collect(Collectors.toSet());

        PlanData validPlanData = validatePlanAndPlanMeals(subscriptionRequestDto);

        subscription.updateMealSelections(validPlanData.planMealIds().stream().map(PlanMealId::new).toList());
        log.info("Subscription with id: {} is updated", subscriptionId.getValue());

        Subscription savedSubscription = persist(subscription, false);

        Set<UUID> planMealIdsAfter = savedSubscription.getMealSelections().stream()
                                                      .map(mealSelection -> mealSelection.getId().getPlanMealId().getValue())
                                                      .collect(Collectors.toSet());

        Set<UUID> unappliedPlanMealIds = new HashSet<>(planMealIdsBefore);
        unappliedPlanMealIds.removeAll(planMealIdsAfter);

        Set<UUID> appliedPlanMealIds = new HashSet<>(planMealIdsAfter);
        appliedPlanMealIds.removeAll(planMealIdsBefore);
        SubscriptionResponseDto subscriptionResponseDto = subscriptionDataMapper.mapSubscriptionToSubscriptionDto(savedSubscription);

        Supplier<Message> eventCreationSupplier =
                () -> USER_PLAN_SUBSCRIPTION_EVENT_CREATION_FUNCTION.execute(subscriptionResponseDto,
                                                                             appliedPlanMealIds.stream().toList(),
                                                                             unappliedPlanMealIds.stream().toList());
        createOutboxMessageAndPersist(subscriptionResponseDto, eventCreationSupplier, DATA_CHANGED, subscriptionConfigData.userSubscriptionTopicName());

        return subscriptionResponseDto;
    }

    @Override
    @Transactional
    @PostAuthorize("returnObject.userId.toString() == principal.toString()")
    @HandleAuthorizationDenied(handlerClass = SubscriptionServiceAccessDeniedHandler.class)
    public SubscriptionResponseDto cancelSubscription(SubscriptionId subscriptionId) {
        log.info("Cancelling subscription with id: {}", subscriptionId);
        Subscription subscription = subscriptionRepository.findBySubscriptionId(subscriptionId.getValue())
                                                          .orElseThrow(() -> new SubscriptionDomainException(SubscriptionErrorCode.INVALID_SUBSCRIPTION_ID));
        subscription.cancel();
        log.info("Subscription with id: {} is cancelled", subscriptionId.getValue());

        Subscription savedSubscription = persist(subscription, false);
        List<UUID> unappliedPlanMealIds = savedSubscription.getMealSelections().stream()
                                                           .map(mealSelection -> mealSelection.getId().getPlanMealId().getValue())
                                                           .toList();

        SubscriptionResponseDto subscriptionResponseDto = subscriptionDataMapper.mapSubscriptionToSubscriptionDto(savedSubscription);
        Supplier<Message> eventCreationSupplier =
                () -> USER_PLAN_SUBSCRIPTION_EVENT_CREATION_FUNCTION.execute(subscriptionResponseDto,
                                                                             Collections.emptyList(),
                                                                             unappliedPlanMealIds);
        createOutboxMessageAndPersist(subscriptionResponseDto, eventCreationSupplier, DATA_CHANGED, subscriptionConfigData.userSubscriptionTopicName());

        return subscriptionResponseDto;
    }

    @Override
    @Transactional
    public List<SubscriptionResponseDto> reflectPlanChanged(PlanId planId, PlanStatus planStatus) {
        List<Subscription> subscriptions = fetchSubscriptionsByPlanId(planId);
        List<SubscriptionResponseDto> subscriptionResponseDtos = new ArrayList<>();

        for (Subscription subscription : subscriptions) {
            switch (planStatus) {
                case ACTIVE -> subscription.activate();
                case CANCELLED -> subscription.cancel();
                case SUSPENDED -> subscription.suspend();
            }
            Subscription updatedSubscription = persist(subscription, false);
            subscriptionResponseDtos.add(subscriptionDataMapper.mapSubscriptionToSubscriptionDto(updatedSubscription));
            log.info("Status of subscription with id: {} is updated", updatedSubscription.getId().getValue());
        }
        return subscriptionResponseDtos;
    }

    @Override
    @Transactional
    public List<SubscriptionResponseDto> reflectPlanMealsRemoved(PlanId planId, List<PlanMealId> planMealIds) {
        List<Subscription> subscriptions = fetchSubscriptionsByPlanId(planId);
        List<SubscriptionResponseDto> subscriptionResponseDtos = new ArrayList<>();

        for (Subscription subscription : subscriptions) {
            subscription.removeMealSelections(planMealIds);
            Subscription updatedSubscription = persist(subscription, false);

            subscriptionResponseDtos.add(subscriptionDataMapper.mapSubscriptionToSubscriptionDto(updatedSubscription));
            log.info("Meal selections of subscription with id: {} is updated", updatedSubscription.getId().getValue());
        }
        return subscriptionResponseDtos;
    }

    @Override
    @Transactional
    public void createOutboxMessageAndPersist(SubscriptionResponseDto subscription,
                                              Supplier<Message> eventSupplier,
                                              String outboxMsgType,
                                              String topicName) {
        log.info("Creating outbox event for subscription with id: {}.",
                 subscription.getUserId().toString());

        UserPlanSubscriptionEventOutboxMessage userPlanSubscriptionEventOutboxMessage =
                UserPlanSubscriptionEventOutboxMessage.builder()
                                                      .id(UUID.randomUUID())
                                                      .userId(subscription.getUserId())
                                                      .topicName(topicName)
                                                      .type(outboxMsgType)
                                                      .outboxStatus(OutboxStatus.STARTED)
                                                      .createdAt(Instant.now())
                                                      .payload(eventSupplier.get())
                                                      .build();

        userPlanSubscriptionEventOutboxRepository.save(userPlanSubscriptionEventOutboxMessage);
        log.info("Outbox event: {} for subscription with id: {} was created.",
                 userPlanSubscriptionEventOutboxMessage,
                 subscription.getSubscriptionId().toString());
    }

    private List<Subscription> fetchSubscriptionsByPlanId(PlanId planId) {
        log.info("Fetching subscriptions with plan id: {}", planId.toString());
        return subscriptionRepository.findByPlanId(planId.getValue());
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
}
