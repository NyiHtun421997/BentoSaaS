package com.nyihtuun.bentosystem.userservice.service;

import com.nyihtuun.bentosystem.userservice.dto.user.UserRequestDTO;
import com.nyihtuun.bentosystem.userservice.dto.user.UserResponseDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponseDTO createUser(UserRequestDTO userRequestDTO);
    UserResponseDTO getUserById(UUID userId);
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO updateUser(UUID userId, UserRequestDTO userRequestDTO);
    void deleteUser(UUID userId);
}
