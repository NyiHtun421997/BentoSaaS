package com.nyihtuun.bentosystem.subscriptionservice.data_access.jpa_repository;

import com.nyihtuun.bentosystem.subscriptionservice.data_access.jpa_entity.SubscriptionEntity;
import com.nyihtuun.bentosystem.subscriptionservice.domain.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionJpaRepository extends JpaRepository<SubscriptionEntity, UUID> {
    List<SubscriptionEntity> findAllByUserIdAndAppliedAtAfter(UUID userId, LocalDateTime since);
}
