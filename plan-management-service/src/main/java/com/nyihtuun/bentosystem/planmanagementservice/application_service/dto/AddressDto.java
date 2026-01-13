package com.nyihtuun.bentosystem.planmanagementservice.application_service.dto;

import com.nyihtuun.bentosystem.domain.valueobject.GeoPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Getter
@Builder
@AllArgsConstructor
public class AddressDto {
    @Size(max = 100)
    private String buildingNameRoomNo;

    @NotBlank
    @Size(max = 50)
    private String chomeBanGo;

    @NotBlank
    @Size(max = 50)
    private String district;

    @NotBlank
    @Pattern(regexp = "\\d{3}-\\d{4}")
    private String postalCode;

    @NotBlank
    @Size(max = 50)
    private String city;

    @NotBlank
    @Size(max = 50)
    private String prefecture;

    @NotNull
    private GeoPoint location;
}
