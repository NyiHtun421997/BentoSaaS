package com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_repository;

import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity.DeliveryScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryScheduleJpaRepository extends JpaRepository<DeliveryScheduleEntity, UUID> {

    @Query("select d from DeliveryScheduleEntity d join d.planEntity p where p.id = :planId and d.createdAt between :from and :to")
    Optional<DeliveryScheduleEntity> findDeliveryScheduleByPlanIdAndCreatedAtBetween(@Param("planId") UUID planId,
                                                                                     @Param("from") Instant from,
                                                                                     @Param("to") Instant to);
}
