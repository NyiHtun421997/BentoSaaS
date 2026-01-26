package com.nyihtuun.bentosystem.planmanagementservice.data_access.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.repository.JobRunRepository;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity.JobRunEntity;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity.JobRunStatus;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_repository.JobRunJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
public class JobRunRepositoryImpl implements JobRunRepository {

    private final JobRunJpaRepository jobRunJpaRepository;
    private final ObjectMapper objectMapper;

    public JobRunRepositoryImpl(JobRunJpaRepository jobRunJpaRepository, ObjectMapper objectMapper) {
        this.jobRunJpaRepository = jobRunJpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UUID startRun(String jobType, LocalDate periodStart, LocalDate periodEnd, LocalDateTime startedAt) {
        UUID id = UUID.randomUUID();
        ArrayNode emptyArrayNode = objectMapper.createArrayNode();

        JobRunEntity jobRunEntity = JobRunEntity.builder()
                                                .id(id)
                                                .jobType(jobType)
                                                .periodStart(periodStart)
                                                .periodEnd(periodEnd)
                                                .status(JobRunStatus.FAILED) // temporary default; overwritten on finish
                                                .startedAt(startedAt)
                                                .createdAt(LocalDateTime.now())
                                                .totalTargets(0)
                                                .successCount(0)
                                                .failureCount(0)
                                                .results(emptyArrayNode)
                                                .build();
        log.info("[StartRun] : Saving job_run: {}", jobRunEntity);
        jobRunJpaRepository.save(jobRunEntity);
        log.info("[StartRun] : Saved job_run: {}", jobRunEntity);
        return id;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finishRun(UUID jobRunId,
                          JobRunStatus status,
                          int totalTargets,
                          int successCount,
                          int failureCount,
                          JsonNode results,
                          JsonNode error,
                          LocalDateTime finishedAt) {

        JobRunEntity jobRunEntity = jobRunJpaRepository.findById(jobRunId)
                                                       .orElseThrow(() -> new IllegalStateException("job_run not found: " + jobRunId));

        JobRunEntity updated = JobRunEntity.builder()
                                           .id(jobRunEntity.getId())
                                           .jobType(jobRunEntity.getJobType())
                                           .periodStart(jobRunEntity.getPeriodStart())
                                           .periodEnd(jobRunEntity.getPeriodEnd())
                                           .startedAt(jobRunEntity.getStartedAt())
                                           .createdAt(jobRunEntity.getCreatedAt())
                                           .status(status)
                                           .totalTargets(totalTargets)
                                           .successCount(successCount)
                                           .failureCount(failureCount)
                                           .results(results)
                                           .error(error)
                                           .finishedAt(finishedAt)
                                           .build();
        log.info("[FinishRun] : Updating job_run: {}", updated);
        jobRunJpaRepository.save(updated);
        log.info("[FinishRun] : Updated job_run: {}", updated);
    }
}
