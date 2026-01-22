package com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.repository;

import com.nyihtuun.bentosystem.subscriptionservice.domain.entity.Subscription;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SubscriptionRepository {
    List<Subscription> findAllSubscriptionsByUserIdAndDate(UUID userId, LocalDate since);
    Subscription findBySubscriptionId(UUID subscriptionId);
    Subscription save(Subscription subscription);
}
