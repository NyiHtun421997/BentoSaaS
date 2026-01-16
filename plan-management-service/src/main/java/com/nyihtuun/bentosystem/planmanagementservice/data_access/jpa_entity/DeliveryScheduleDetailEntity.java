package com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "delivery_schedule_detail", schema = "planmanagement")
@Entity
public class DeliveryScheduleDetailEntity {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_meal_id")
    private PlanMealEntity planMealEntity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "delivery_schedule_id", nullable = false)
    private DeliveryScheduleEntity deliveryScheduleEntity;

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;
}
