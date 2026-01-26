package com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.repository;

import com.nyihtuun.bentosystem.subscriptionservice.domain.entity.Subscription;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository {
    List<Subscription> findAllSubscriptionsByUserIdAndDate(UUID userId, LocalDate since);
    List<Subscription> findActiveSubscriptionsBeforeDate(LocalDateTime before);
    Optional<Subscription> findBySubscriptionId(UUID subscriptionId);
    Subscription save(Subscription subscription, boolean flush);
}
