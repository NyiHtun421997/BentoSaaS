package com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_repository;

import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity.JobRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface JobRunJpaRepository extends JpaRepository<JobRunEntity, UUID> {

    Optional<JobRunEntity> findByJobTypeAndPeriodStartAndPeriodEnd(
            String jobType, LocalDate periodStart, LocalDate periodEnd
    );
}
