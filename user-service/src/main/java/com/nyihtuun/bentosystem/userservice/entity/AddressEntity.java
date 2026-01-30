package com.nyihtuun.bentosystem.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "address", schema = "userinfo")
public class AddressEntity {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "building_name_room_no", nullable = false)
    private String buildingNameRoomNo;

    @Column(name = "chome_ban_go", nullable = false)
    private String chomeBanGo;

    @Column(name = "district", nullable = false)
    private String district;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "prefecture", nullable = false)
    private String prefecture;
}
