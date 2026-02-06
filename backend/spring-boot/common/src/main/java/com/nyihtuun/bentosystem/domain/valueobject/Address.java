package com.nyihtuun.bentosystem.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public final class Address {
    private String buildingNameRoomNo;
    private String chomeBanGo;
    private String district;
    private String postalCode;
    private String city;
    private String prefecture;
    private GeoPoint location;
}
