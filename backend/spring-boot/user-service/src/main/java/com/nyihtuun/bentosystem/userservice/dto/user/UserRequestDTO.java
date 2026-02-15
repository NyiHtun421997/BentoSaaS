package com.nyihtuun.bentosystem.userservice.dto.user;

import com.nyihtuun.bentosystem.userservice.dto.validation.PhoneNumber;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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
public class UserRequestDTO {

    @NotBlank(message = "{NotBlank.userRequestDto.email}")
    @Email(message = "{Email.userRequestDto.email}")
    private String email;

    @NotBlank(message = "{NotBlank.userRequestDto.password}")
    @Size(min = 8, message = "{Size.userRequestDto.password}")
    private String password;

    private String firstName;

    private String lastName;

    @PhoneNumber(message = "{PhoneNumber.userRequestDto.phNo}")
    private String phNo;
    
    private String description;

    @Size(max = 255, message = "{Size.planRequestDto.imageUrl}")
    @Pattern(regexp = "^(https?://).*$", message = "{Pattern.planRequestDto.imageUrl}")
    private String imageUrl;

    @Valid
    private AddressRequestDTO address;
}
