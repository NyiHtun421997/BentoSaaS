package com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity.JobRunStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public interface JobRunRepository {

    UUID startRun(String jobType, LocalDate periodStart, LocalDate periodEnd, Instant startedAt);

    void finishRun(UUID jobRunId,
                   JobRunStatus status,
                   int totalTargets,
                   int successCount,
                   int failureCount,
                   JsonNode results,
                   JsonNode error,
                   Instant finishedAt);
}
