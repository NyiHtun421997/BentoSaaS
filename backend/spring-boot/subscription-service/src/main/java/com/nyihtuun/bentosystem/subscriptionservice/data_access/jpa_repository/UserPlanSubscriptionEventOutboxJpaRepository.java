package com.nyihtuun.bentosystem.subscriptionservice.data_access.jpa_repository;

import com.nyihtuun.bentosystem.domain.valueobject.status.OutboxStatus;
import com.nyihtuun.bentosystem.subscriptionservice.data_access.jpa_entity.UserPlanSubscriptionEventOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserPlanSubscriptionEventOutboxJpaRepository extends JpaRepository<UserPlanSubscriptionEventOutboxEntity, UUID> {
    List<UserPlanSubscriptionEventOutboxEntity> findAllByOutboxStatus(OutboxStatus outboxStatus);
}
