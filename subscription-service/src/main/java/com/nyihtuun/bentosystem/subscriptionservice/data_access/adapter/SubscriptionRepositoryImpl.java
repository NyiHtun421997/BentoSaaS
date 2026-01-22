package com.nyihtuun.bentosystem.subscriptionservice.data_access.adapter;

import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.repository.SubscriptionRepository;
import com.nyihtuun.bentosystem.subscriptionservice.domain.entity.Subscription;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class SubscriptionRepositoryImpl implements SubscriptionRepository {
    @Override
    public List<Subscription> findAllSubscriptionsByUserIdAndDate(UUID userId, LocalDate since) {
        return List.of();
    }

    @Override
    public Subscription findBySubscriptionId(UUID subscriptionId) {
        return null;
    }

    @Override
    public Subscription save(Subscription subscription) {
        return null;
    }
}
