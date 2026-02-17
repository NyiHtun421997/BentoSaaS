package com.nyihtuun.bentosystem.userservice.service;

import com.nyihtuun.bentosystem.userservice.dto.user.SignupRequestDTO;
import com.nyihtuun.bentosystem.userservice.dto.user.UserRequestDTO;
import com.nyihtuun.bentosystem.userservice.dto.user.UserResponseDTO;
import com.nyihtuun.bentosystem.userservice.entity.AddressEntity;
import com.nyihtuun.bentosystem.userservice.entity.RoleEntity;
import com.nyihtuun.bentosystem.userservice.entity.UserEntity;
import com.nyihtuun.bentosystem.userservice.exception.UserServiceErrorCode;
import com.nyihtuun.bentosystem.userservice.exception.UserServiceException;
import com.nyihtuun.bentosystem.userservice.mapper.UserMapper;
import com.nyihtuun.bentosystem.userservice.repository.RoleRepository;
import com.nyihtuun.bentosystem.userservice.repository.UserRepository;
import com.nyihtuun.bentosystem.userservice.security.authorization_handler.AdminAccessDeniedAuthorizationHandler;
import com.nyihtuun.bentosystem.userservice.security.authorization_handler.GenericAccessDeniedAuthorizationHandler;
import com.nyihtuun.bentosystem.userservice.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.method.HandleAuthorizationDenied;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    @Transactional
    public UserResponseDTO createUser(SignupRequestDTO signupRequestDTO, boolean isProvider) {
        log.info("Creating user: {}", signupRequestDTO);
        UserEntity userEntity = userMapper.toEntity(signupRequestDTO);
        userEntity.setUserId(UUID.randomUUID());
        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(signupRequestDTO.getPassword()));

        RoleEntity regularUser = roleRepository.findByName(RoleEntity.Role.ROLE_USER);

        Set<RoleEntity> roleEntities = new HashSet<>(Set.of(regularUser));
        userEntity.setRoleEntities(roleEntities);

        if (isProvider) {
            RoleEntity provider = roleRepository.findByName(RoleEntity.Role.ROLE_PROVIDER);
            userEntity.getRoleEntities().add(provider);
        }

        Instant now = Instant.now();
        userEntity.setCreatedAt(now);
        userEntity.setUpdatedAt(now);

        UserEntity savedUser = null;
        try {
            savedUser = userRepository.saveAndFlush(userEntity);
            log.info("User created successfully: {}", savedUser);
            return userMapper.toResponseDTO(savedUser);
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to save user: {}", signupRequestDTO, e);
            throw new UserServiceException(UserServiceErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @HandleAuthorizationDenied(handlerClass = GenericAccessDeniedAuthorizationHandler.class)
    public UserResponseDTO getUserById(UUID userId) {
        log.info("Fetching user with id: {}", userId);
        UserEntity userEntity = userRepository.findById(userId)
                                              .orElseThrow(() -> new UserServiceException(UserServiceErrorCode.USER_NOT_FOUND));
        log.info("User fetched successfully: {}", userEntity);
        return userMapper.toResponseDTO(userEntity);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    @HandleAuthorizationDenied(handlerClass = AdminAccessDeniedAuthorizationHandler.class)
    public List<UserResponseDTO> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
                             .map(userMapper::toResponseDTO)
                             .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @PreAuthorize("principal.toString() == #userId.toString() or hasRole('ADMIN')")
    @HandleAuthorizationDenied(handlerClass = GenericAccessDeniedAuthorizationHandler.class)
    public UserResponseDTO updateUser(UUID userId, UserRequestDTO userRequestDTO) {
        log.info("Updating user: {}", userRequestDTO);
        UserEntity userEntity = userRepository.findById(userId)
                                              .orElseThrow(() -> new UserServiceException(UserServiceErrorCode.USER_NOT_FOUND));

        userEntity.setFirstName(userRequestDTO.getFirstName());
        userEntity.setLastName(userRequestDTO.getLastName());
        userEntity.setPhNo(userRequestDTO.getPhNo());
        userEntity.setDescription(userRequestDTO.getDescription());
        userEntity.setImageKey(userRequestDTO.getImageKey());
        userEntity.setUpdatedAt(Instant.now());

        if (userRequestDTO.getAddress() != null) {
            AddressEntity addressEntity = userEntity.getAddressEntity();
            if (addressEntity == null) {
                addressEntity = userMapper.toAddressEntity(userRequestDTO.getAddress());
                userEntity.setAddressEntity(addressEntity);
                log.debug("Address was null, created new address entity: {}", addressEntity);
            } else {
                addressEntity.setBuildingNameRoomNo(userRequestDTO.getAddress().getBuildingNameRoomNo());
                addressEntity.setChomeBanGo(userRequestDTO.getAddress().getChomeBanGo());
                addressEntity.setDistrict(userRequestDTO.getAddress().getDistrict());
                addressEntity.setPostalCode(userRequestDTO.getAddress().getPostalCode());
                addressEntity.setCity(userRequestDTO.getAddress().getCity());
                addressEntity.setPrefecture(userRequestDTO.getAddress().getPrefecture());
                log.debug("Address was not null, updated address entity: {}", addressEntity);
            }
        } else {
            log.debug("Address is null");
            userEntity.setAddressEntity(null);
        }

        UserEntity updatedUser = null;
        try {
            updatedUser = userRepository.saveAndFlush(userEntity);
            log.info("User updated successfully: {}", updatedUser);
            return userMapper.toResponseDTO(updatedUser);
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to save user: {}", userRequestDTO, e);
            throw new UserServiceException(UserServiceErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("principal.toString() == #userId.toString() or hasRole('ADMIN')")
    @HandleAuthorizationDenied(handlerClass = GenericAccessDeniedAuthorizationHandler.class)
    public void deleteUser(UUID userId) {
        log.info("Deleting user with id: {}", userId);
        if (!userRepository.existsById(userId)) {
            log.error("User with id {} not found", userId);
            throw new UserServiceException(UserServiceErrorCode.USER_NOT_FOUND);
        }
        userRepository.deleteById(userId);
        log.info("User deleted successfully");
    }

    @Override
    @NullMarked
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);
        UserEntity userEntity = userRepository.findByEmail(username)
                                              .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<SimpleGrantedAuthority> authorities = userEntity.getRoleEntities()
                                                             .stream()
                                                             .map(roleEntity -> new SimpleGrantedAuthority(roleEntity.getName()
                                                                                                                     .name()))
                                                             .toList();

        log.info("User loaded successfully: {}", userEntity);
        return new UserPrincipal(userEntity.getUserId().toString(),
                        userEntity.getEncryptedPassword(),
                        true,
                        true,
                        true,
                        true,
                        userEntity.getEmail(),
                        authorities);
    }
}
