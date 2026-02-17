package com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity;

import com.nyihtuun.bentosystem.domain.valueobject.status.PlanStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"addressEntity", "categoryEntities", "planMealEntities"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "plan", schema = "planmanagement")
@Entity
public class PlanEntity {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "code", columnDefinition = "char(7)", length = 7, nullable = false)
    private String code;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "plan_status", nullable = false)
    private PlanStatus planStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "image_key")
    private String imageKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "skip_dates", columnDefinition = "jsonb", nullable = false)
    private List<LocalDate> skipDates = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id")
    private AddressEntity addressEntity;

    @ManyToMany
    @JoinTable(
            schema = "planmanagement",
            name = "plan_category",
            joinColumns = @JoinColumn(name = "plan_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<CategoryEntity> categoryEntities;

    @Column(
            name = "display_subscription_fee",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal displaySubscriptionFee;

    @Column(name = "delete_flag")
    private Boolean deleteFlag;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @OneToMany(
            mappedBy = "planEntity",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PlanMealEntity> planMealEntities;
}
