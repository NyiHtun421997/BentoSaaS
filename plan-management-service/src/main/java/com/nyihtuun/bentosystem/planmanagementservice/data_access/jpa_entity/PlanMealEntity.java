package com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "plan_meal", schema = "planmanagement")
@Entity
public class PlanMealEntity {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private PlanEntity planEntity;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price_per_month",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal pricePerMonth;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    @Column(name = "min_sub_count")
    private Integer minSubCount;

    @Column(name = "current_sub_count")
    private Integer currentSubCount;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "delete_flag")
    private Boolean deleteFlag;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
