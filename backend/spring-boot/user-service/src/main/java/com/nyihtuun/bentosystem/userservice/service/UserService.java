package com.nyihtuun.bentosystem.userservice.service;

import com.nyihtuun.bentosystem.userservice.dto.user.SignupRequestDTO;
import com.nyihtuun.bentosystem.userservice.dto.user.UserRequestDTO;
import com.nyihtuun.bentosystem.userservice.dto.user.UserResponseDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.UUID;

public interface UserService extends UserDetailsService {
    UserResponseDTO createUser(SignupRequestDTO signupRequestDTO, boolean isProvider);
    UserResponseDTO getUserById(UUID userId);
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO updateUser(UUID userId, UserRequestDTO userRequestDTO);
    void deleteUser(UUID userId);
}
