package com.nyihtuun.bentosystem.userservice.controller;

import com.nyihtuun.bentosystem.userservice.dto.user.SignupRequestDTO;
import com.nyihtuun.bentosystem.userservice.dto.user.UserRequestDTO;
import com.nyihtuun.bentosystem.userservice.dto.user.UserResponseDTO;
import com.nyihtuun.bentosystem.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.nyihtuun.bentosystem.userservice.controller.ApiPaths.*;

@RestController
@RequestMapping(VERSION1)
@RequiredArgsConstructor
@Tag(name = "User", description = "Endpoints for user management, including signup and profile operations.")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup/regular")
    @Operation(summary = "Register a regular user", description = "Creates a new user account with the ROLE_USER role.")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    public ResponseEntity<UserResponseDTO> createRegularUser(@Valid @RequestBody SignupRequestDTO signupRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(signupRequestDTO, false));
    }

    @PostMapping("/signup/provider")
    @Operation(summary = "Register a provider user", description = "Creates a new user account with both ROLE_USER and ROLE_PROVIDER roles.")
    @ApiResponse(responseCode = "201", description = "Provider user created successfully")
    public ResponseEntity<UserResponseDTO> createProviderUser(@Valid @RequestBody SignupRequestDTO signupRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(signupRequestDTO, true));
    }

    @GetMapping(USER_ID)
    @Operation(summary = "Get user by ID", description = "Retrieves detailed profile information for a specific user.")
    @ApiResponse(responseCode = "200", description = "User details retrieved successfully")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves a list of all registered users. Restricted to admin users.")
    @ApiResponse(responseCode = "200", description = "List of users retrieved successfully")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping(USER_ID)
    @Operation(summary = "Update user profile", description = "Updates the profile information (email, name, address, etc.) for an existing user.")
    @ApiResponse(responseCode = "200", description = "User profile updated successfully")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable UUID userId, @Valid @RequestBody UserRequestDTO userRequestDTO) {
        return ResponseEntity.ok(userService.updateUser(userId, userRequestDTO));
    }

    @DeleteMapping(USER_ID)
    @Operation(summary = "Delete user", description = "Permanently removes a user account from the system.")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
