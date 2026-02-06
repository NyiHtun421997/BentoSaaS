package com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "plans")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "category", schema = "planmanagement")
@Entity
public class CategoryEntity {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToMany(mappedBy = "categoryEntities", fetch = FetchType.LAZY)
    private Set<PlanEntity> plans;
}
