package com.nyihtuun.bentosystem.userservice.service;

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
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.method.HandleAuthorizationDenied;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
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

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO, boolean isProvider) {
        UserEntity userEntity = userMapper.toEntity(userRequestDTO);
        userEntity.setUserId(UUID.randomUUID());
        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(userRequestDTO.getPassword()));

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

        UserEntity savedUser = userRepository.save(userEntity);
        return userMapper.toResponseDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("principal.toString() == #userId.toString() or hasRole('ADMIN')")
    @HandleAuthorizationDenied(handlerClass = GenericAccessDeniedAuthorizationHandler.class)
    public UserResponseDTO getUserById(UUID userId) {
        UserEntity userEntity = userRepository.findById(userId)
                                              .orElseThrow(() -> new UserServiceException(UserServiceErrorCode.USER_NOT_FOUND));
        return userMapper.toResponseDTO(userEntity);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    @HandleAuthorizationDenied(handlerClass = AdminAccessDeniedAuthorizationHandler.class)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                             .map(userMapper::toResponseDTO)
                             .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @PreAuthorize("principal.toString() == #userId.toString() or hasRole('ADMIN')")
    @HandleAuthorizationDenied(handlerClass = GenericAccessDeniedAuthorizationHandler.class)
    public UserResponseDTO updateUser(UUID userId, UserRequestDTO userRequestDTO) {
        UserEntity userEntity = userRepository.findById(userId)
                                              .orElseThrow(() -> new UserServiceException(UserServiceErrorCode.USER_NOT_FOUND));

        userEntity.setEmail(userRequestDTO.getEmail());
        userEntity.setFirstName(userRequestDTO.getFirstName());
        userEntity.setLastName(userRequestDTO.getLastName());
        userEntity.setPhNo(userRequestDTO.getPhNo());
        userEntity.setDescription(userRequestDTO.getDescription());
        userEntity.setUpdatedAt(Instant.now());

        if (userRequestDTO.getAddress() != null) {
            AddressEntity addressEntity = userEntity.getAddressEntity();
            if (addressEntity == null) {
                addressEntity = userMapper.toAddressEntity(userRequestDTO.getAddress());
                addressEntity.setId(UUID.randomUUID());
                userEntity.setAddressEntity(addressEntity);
            } else {
                addressEntity.setBuildingNameRoomNo(userRequestDTO.getAddress().getBuildingNameRoomNo());
                addressEntity.setChomeBanGo(userRequestDTO.getAddress().getChomeBanGo());
                addressEntity.setDistrict(userRequestDTO.getAddress().getDistrict());
                addressEntity.setPostalCode(userRequestDTO.getAddress().getPostalCode());
                addressEntity.setCity(userRequestDTO.getAddress().getCity());
                addressEntity.setPrefecture(userRequestDTO.getAddress().getPrefecture());
            }
        } else {
            userEntity.setAddressEntity(null);
        }

        UserEntity updatedUser = userRepository.save(userEntity);
        return userMapper.toResponseDTO(updatedUser);
    }

    @Override
    @Transactional
    @PreAuthorize("principal.toString() == #userId.toString() or hasRole('ADMIN')")
    @HandleAuthorizationDenied(handlerClass = GenericAccessDeniedAuthorizationHandler.class)
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserServiceException(UserServiceErrorCode.USER_NOT_FOUND);
        }
        userRepository.deleteById(userId);
    }

    @Override
    @NullMarked
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(username)
                                              .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<SimpleGrantedAuthority> authorities = userEntity.getRoleEntities()
                                                             .stream()
                                                             .map(roleEntity -> new SimpleGrantedAuthority(roleEntity.getName()
                                                                                                                     .name()))
                                                             .toList();

        return new User(userEntity.getUserId().toString(),
                        userEntity.getEncryptedPassword(),
                        true,
                        true,
                        true,
                        true,
                        authorities);
    }
}
