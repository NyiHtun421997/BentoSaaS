package com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Table(name = "job_run", schema = "planmanagement")
@Entity
public class JobRunEntity {

    @Id
    private UUID id;

    @Column(name = "job_type", nullable = false, length = 100)
    private String jobType;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private JobRunStatus status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "total_targets", nullable = false)
    private int totalTargets;

    @Column(name = "success_count", nullable = false)
    private int successCount;

    @Column(name = "failure_count", nullable = false)
    private int failureCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "results", nullable = false, columnDefinition = "jsonb")
    private JsonNode results;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "error", columnDefinition = "jsonb")
    private JsonNode error;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}
