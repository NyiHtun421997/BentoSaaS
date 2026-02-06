package com.nyihtuun.bentosystem.domain.dto;

import com.nyihtuun.bentosystem.domain.valueobject.GeoPoint;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class AddressDto {
    @Size(max = 100, message = "{Size.addressDto.buildingNameRoomNo}")
    private String buildingNameRoomNo;

    @NotBlank(message = "{NotBlank.addressDto.chomeBanGo}")
    @Size(max = 50, message = "{Size.addressDto.chomeBanGo}")
    private String chomeBanGo;

    @NotBlank(message = "{NotBlank.addressDto.district}")
    @Size(max = 50, message = "{Size.addressDto.district}")
    private String district;

    @NotBlank(message = "{NotBlank.addressDto.postalCode}")
    @Pattern(regexp = "\\d{3}-\\d{4}", message = "{Pattern.addressDto.postalCode}")
    private String postalCode;

    @NotBlank(message = "{NotBlank.addressDto.city}")
    @Size(max = 50, message = "{Size.addressDto.city}")
    private String city;

    @NotBlank(message = "{NotBlank.addressDto.prefecture}")
    @Size(max = 50, message = "{Size.addressDto.prefecture}")
    private String prefecture;

    @NotNull(message = "{NotNull.addressDto.location}")
    private GeoPoint location;
}
