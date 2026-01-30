package com.nyihtuun.bentosystem.userservice.dto.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginRequestDto {
    @NotBlank(message = "{NotBlank.loginRequestDto.email}")
    @Email(message = "{Email.loginRequestDto.email}")
    private String email;

    @NotBlank(message = "{NotBlank.loginRequestDto.password}")
    @Size(min = 8, message = "{Size.loginRequestDto.password}")
    private String password;
}
