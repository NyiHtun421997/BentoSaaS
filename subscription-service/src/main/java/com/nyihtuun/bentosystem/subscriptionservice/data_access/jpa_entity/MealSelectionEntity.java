package com.nyihtuun.bentosystem.subscriptionservice.data_access.jpa_entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "subscription")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@IdClass(MealSelectionEntityId.class)
@Table(name = "meal_selection", schema = "subscription")
@Entity
public class MealSelectionEntity {

    @Id
    @EqualsAndHashCode.Include
    @ManyToOne(optional = false)
    @JoinColumn(name = "subscription_id", nullable = false)
    private SubscriptionEntity subscription;

    @Id
    @Column(name = "plan_meal_id", nullable = false)
    private UUID planMealId;
}
