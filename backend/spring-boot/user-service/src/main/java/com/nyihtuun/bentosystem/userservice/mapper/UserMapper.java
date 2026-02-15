package com.nyihtuun.bentosystem.userservice.mapper;

import com.nyihtuun.bentosystem.userservice.dto.user.*;
import com.nyihtuun.bentosystem.userservice.entity.AddressEntity;
import com.nyihtuun.bentosystem.userservice.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDTO toResponseDTO(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }

        return UserResponseDTO.builder()
                .userId(userEntity.getUserId())
                .email(userEntity.getEmail())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .phNo(userEntity.getPhNo())
                .description(userEntity.getDescription())
                .imageUrl(userEntity.getImageUrl())
                .joinedOn(userEntity.getCreatedAt())
                .updatedAt(userEntity.getUpdatedAt())
                .address(toAddressResponseDTO(userEntity.getAddressEntity()))
                .build();
    }

    public AddressResponseDTO toAddressResponseDTO(AddressEntity addressEntity) {
        if (addressEntity == null) {
            return null;
        }

        return AddressResponseDTO.builder()
                .id(addressEntity.getId())
                .buildingNameRoomNo(addressEntity.getBuildingNameRoomNo())
                .chomeBanGo(addressEntity.getChomeBanGo())
                .district(addressEntity.getDistrict())
                .postalCode(addressEntity.getPostalCode())
                .city(addressEntity.getCity())
                .prefecture(addressEntity.getPrefecture())
                .build();
    }

    public UserEntity toEntity(UserRequestDTO userRequestDTO) {
        if (userRequestDTO == null) {
            return null;
        }

        return UserEntity.builder()
                .email(userRequestDTO.getEmail())
                .firstName(userRequestDTO.getFirstName())
                .lastName(userRequestDTO.getLastName())
                .phNo(userRequestDTO.getPhNo())
                .description(userRequestDTO.getDescription())
                .imageUrl(userRequestDTO.getImageUrl())
                .addressEntity(toAddressEntity(userRequestDTO.getAddress()))
                .build();
    }

    public UserEntity toEntity(SignupRequestDTO signupRequestDTO) {
        if (signupRequestDTO == null) {
            return null;
        }

        return UserEntity.builder()
                .email(signupRequestDTO.getEmail())
                .build();
    }

    public AddressEntity toAddressEntity(AddressRequestDTO addressRequestDTO) {
        if (addressRequestDTO == null) {
            return null;
        }

        return AddressEntity.builder()
                .buildingNameRoomNo(addressRequestDTO.getBuildingNameRoomNo())
                .chomeBanGo(addressRequestDTO.getChomeBanGo())
                .district(addressRequestDTO.getDistrict())
                .postalCode(addressRequestDTO.getPostalCode())
                .city(addressRequestDTO.getCity())
                .prefecture(addressRequestDTO.getPrefecture())
                .build();
    }
}
