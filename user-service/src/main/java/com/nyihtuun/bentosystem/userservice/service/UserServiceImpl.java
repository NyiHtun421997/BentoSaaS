package com.nyihtuun.bentosystem.userservice.service;

import com.nyihtuun.bentosystem.userservice.dto.user.UserRequestDTO;
import com.nyihtuun.bentosystem.userservice.dto.user.UserResponseDTO;
import com.nyihtuun.bentosystem.userservice.entity.AddressEntity;
import com.nyihtuun.bentosystem.userservice.entity.UserEntity;
import com.nyihtuun.bentosystem.userservice.exception.UserServiceErrorCode;
import com.nyihtuun.bentosystem.userservice.exception.UserServiceException;
import com.nyihtuun.bentosystem.userservice.mapper.UserMapper;
import com.nyihtuun.bentosystem.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        UserEntity userEntity = userMapper.toEntity(userRequestDTO);
        userEntity.setUserId(UUID.randomUUID());
        // For now, setting encrypted password as is (should be encoded in a real scenario)
        userEntity.setEncryptedPassword(userRequestDTO.getPassword());
        
        Instant now = Instant.now();
        userEntity.setCreatedAt(now);
        userEntity.setUpdatedAt(now);

        UserEntity savedUser = userRepository.save(userEntity);
        return userMapper.toResponseDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(UUID userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserServiceException(UserServiceErrorCode.USER_NOT_FOUND));
        return userMapper.toResponseDTO(userEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
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
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserServiceException(UserServiceErrorCode.USER_NOT_FOUND);
        }
        userRepository.deleteById(userId);
    }
}
