package com.nyihtuun.bentosystem.userservice.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phNo;
    private String description;
    private String image;
    private Instant joinedOn;
    private Instant updatedAt;
    private AddressResponseDTO address;
}
