package com.nyihtuun.bentosystem.userservice.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponseDTO {
    private UUID id;
    private String buildingNameRoomNo;
    private String chomeBanGo;
    private String district;
    private String postalCode;
    private String city;
    private String prefecture;
}
