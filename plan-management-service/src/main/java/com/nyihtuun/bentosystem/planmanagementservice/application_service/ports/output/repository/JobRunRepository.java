package com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.repository;

import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity.JobRunStatus;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface JobRunRepository {

    UUID startRun(String jobType, LocalDate periodStart, LocalDate periodEnd, LocalDateTime startedAt);

    void finishRun(UUID jobRunId,
                   JobRunStatus status,
                   int totalTargets,
                   int successCount,
                   int failureCount,
                   JsonNode results,
                   JsonNode error,
                   LocalDateTime finishedAt);
}
