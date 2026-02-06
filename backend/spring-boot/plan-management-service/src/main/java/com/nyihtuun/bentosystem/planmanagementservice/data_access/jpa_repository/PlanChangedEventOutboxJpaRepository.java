package com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_repository;

import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity.PlanChangedEventOutboxEntity;
import com.nyihtuun.bentosystem.domain.valueobject.status.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlanChangedEventOutboxJpaRepository extends JpaRepository<PlanChangedEventOutboxEntity, UUID> {
    List<PlanChangedEventOutboxEntity> findAllByOutboxStatus(OutboxStatus outboxStatus);
}
