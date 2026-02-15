package com.nyihtuun.bentosystem.userservice.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDTO {

    @NotBlank(message = "{NotBlank.userRequestDto.email}")
    @Email(message = "{Email.userRequestDto.email}")
    private String email;

    @NotBlank(message = "{NotBlank.userRequestDto.password}")
    @Size(min = 8, message = "{Size.userRequestDto.password}")
    private String password;
}
