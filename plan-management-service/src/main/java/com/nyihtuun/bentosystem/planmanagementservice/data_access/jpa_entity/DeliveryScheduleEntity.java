package com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"planEntity", "deliveryScheduleDetailEntities"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "delivery_schedule", schema = "planmanagement")
@Entity
public class DeliveryScheduleEntity {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id")
    private PlanEntity planEntity;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "deliveryScheduleEntity",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<DeliveryScheduleDetailEntity> deliveryScheduleDetailEntities;
}
