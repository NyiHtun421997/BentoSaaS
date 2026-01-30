package com.nyihtuun.bentosystem.userservice.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequestDTO {
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
}
