package com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "address", schema = "planmanagement")
@Entity
public class AddressEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String buildingNameRoomNo;

    @Column(nullable = false)
    private String chomeBanGo;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String prefecture;

    @Column(name = "geo_point", columnDefinition = "geography(Point,4326)")
    private Point location;

    @OneToOne(mappedBy = "addressEntity")
    private PlanEntity planEntity;
}
