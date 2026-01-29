package com.nyihtuun.bentosystem.subscriptionservice.data_access.adapter;

import com.nyihtuun.bentosystem.domain.valueobject.status.SubscriptionStatus;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.repository.SubscriptionRepository;
import com.nyihtuun.bentosystem.subscriptionservice.data_access.jpa_entity.SubscriptionEntity;
import com.nyihtuun.bentosystem.subscriptionservice.data_access.jpa_repository.SubscriptionJpaRepository;
import com.nyihtuun.bentosystem.subscriptionservice.data_access.mapper.SubscriptionDataAccessMapper;
import com.nyihtuun.bentosystem.subscriptionservice.domain.entity.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SubscriptionRepositoryImpl implements SubscriptionRepository {

    private final SubscriptionJpaRepository subscriptionJpaRepository;
    private final SubscriptionDataAccessMapper subscriptionDataMapper;

    @Autowired
    public SubscriptionRepositoryImpl(SubscriptionJpaRepository subscriptionJpaRepository,
                                      SubscriptionDataAccessMapper subscriptionDataMapper) {
        this.subscriptionJpaRepository = subscriptionJpaRepository;
        this.subscriptionDataMapper = subscriptionDataMapper;
    }

    @Override
    public List<Subscription> findAllSubscriptionsByUserIdAndDate(UUID userId, LocalDate since) {
        return subscriptionJpaRepository.findAllByUserIdAndAppliedAtAfter(userId, Instant.from(since.atStartOfDay()))
                .stream()
                .map(subscriptionDataMapper::subscriptionEntityToSubscription)
                .toList();
    }

    @Override
    public List<Subscription> findActiveSubscriptionsBeforeDate(Instant before) {
        return subscriptionJpaRepository.findAllBySubscriptionStatusAndAppliedAtBefore(SubscriptionStatus.SUBSCRIBED, before)
                .stream()
                .map(subscriptionDataMapper::subscriptionEntityToSubscription)
                .toList();
    }

    @Override
    public Optional<Subscription> findBySubscriptionId(UUID subscriptionId) {
        return subscriptionJpaRepository.findById(subscriptionId)
                .map(subscriptionDataMapper::subscriptionEntityToSubscription);
    }

    @Override
    public List<Subscription> findByPlanId(UUID planId) {
        return subscriptionJpaRepository.findAllByPlanId(planId)
                .stream()
                .map(subscriptionDataMapper::subscriptionEntityToSubscription)
                .toList();
    }

    @Override
    public Subscription save(Subscription subscription, boolean flush) {
        SubscriptionEntity subscriptionEntity = subscriptionDataMapper.subscriptionToSubscriptionEntity(subscription);
        SubscriptionEntity savedSubscriptionEntity = subscriptionJpaRepository.save(subscriptionEntity);
        if (flush) {
            subscriptionJpaRepository.flush();
        }
        return subscriptionDataMapper.subscriptionEntityToSubscription(savedSubscriptionEntity);
    }
}
