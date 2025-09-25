package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.userManagement.param.UserFilterParam;
import com.kltn.scsms_api_service.core.dto.userManagement.request.CreateUserRequest;
import com.kltn.scsms_api_service.core.dto.userManagement.UserInfoDto;
import com.kltn.scsms_api_service.core.dto.userManagement.request.UpdateUserRequest;
import com.kltn.scsms_api_service.core.entity.Role;
import com.kltn.scsms_api_service.core.entity.User;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.core.service.entityService.PermissionService;
import com.kltn.scsms_api_service.core.service.entityService.RoleService;
import com.kltn.scsms_api_service.core.service.entityService.UserService;
import com.kltn.scsms_api_service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementService {
    
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    
    private final UserService userService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    
    public Page<UserInfoDto> getAllUsers(UserFilterParam userFilterParam) {
        
        Page<User> userPage = userService.getAllUsersWithFilters(userFilterParam);
        
        return userPage.map(userMapper::toUserInfoDto);
    }
    
    public UserInfoDto createUser(CreateUserRequest createUserRequest) {
        // Validate role exists
        Optional<Role> roleOtp = roleService.getRoleByRoleCode(createUserRequest.getRoleCode());
        if (roleOtp.isEmpty()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Role with code " + createUserRequest.getRoleCode() + " does not exist.");
        }
        
        // Validate email not already in use
        if (userService.findByEmail(createUserRequest.getEmail()).isPresent()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Email " + createUserRequest.getEmail() + " is already in use.");
        }
        
        // Encode password
        String encodedPassword = passwordEncoder.encode(createUserRequest.getPassword());
        
        // Create new user
        User newUser = User.builder()
            .googleId(createUserRequest.getGoogleId())
            .email(createUserRequest.getEmail())
            .password(encodedPassword)
            .fullName(createUserRequest.getFullName())
            .phoneNumber(createUserRequest.getPhoneNumber())
            .dateOfBirth(createUserRequest.getDateOfBirth())
            .gender(createUserRequest.getGender())
            .address(createUserRequest.getAddress())
            .avatarUrl(createUserRequest.getAvatarUrl())
            .role(roleOtp.get())
            .isActive(true)
            .build();
        
        User createdUser = userService.saveUser(newUser);
        
        log.info("Created new user with email: {}", createdUser.getEmail());
        
        return userMapper.toUserInfoDto(createdUser);
    }
    
    public UserInfoDto updateUser(UUID uid, UpdateUserRequest updateUserRequest) {
        // First get existing user
        User existingUser = userService.findById(uid).orElseThrow(() ->
            new ClientSideException(ErrorCode.NOT_FOUND, "User with ID " + uid + " not found."));
        
        // If role code is being updated, validate new role exists
        if (updateUserRequest.getRoleCode() != null && !updateUserRequest.getRoleCode().equals(existingUser.getRole().getRoleCode())) {
            Role newRole = roleService.getRoleByRoleCode(updateUserRequest.getRoleCode()).orElseThrow(() ->
                new ClientSideException(ErrorCode.BAD_REQUEST, "Role with code " + updateUserRequest.getRoleCode() + " does not exist."));
            existingUser.setRole(newRole);
        }
        
        // Update fields if provided
        if (updateUserRequest.getEmail() != null) {
            // Check if new email is already in use by another user
            Optional<User> userWithEmail = userService.findByEmail(updateUserRequest.getEmail());
            if (userWithEmail.isPresent() && !userWithEmail.get().getUserId().equals(existingUser.getUserId())) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, "Email " + updateUserRequest.getEmail() + " is already in use.");
            }
            existingUser.setEmail(updateUserRequest.getEmail());
        }
        if (updateUserRequest.getFullName() != null) {
            existingUser.setFullName(updateUserRequest.getFullName());
        }
        if (updateUserRequest.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(updateUserRequest.getPhoneNumber());
        }
        if (updateUserRequest.getDateOfBirth() != null) {
            existingUser.setDateOfBirth(updateUserRequest.getDateOfBirth());
        }
        if (updateUserRequest.getGender() != null) {
            existingUser.setGender(updateUserRequest.getGender());
        }
        if (updateUserRequest.getAddress() != null) {
            existingUser.setAddress(updateUserRequest.getAddress());
        }
        if (updateUserRequest.getAvatarUrl() != null) {
            existingUser.setAvatarUrl(updateUserRequest.getAvatarUrl());
        }
        if (updateUserRequest.getIsActive() != null) {
            existingUser.setIsActive(updateUserRequest.getIsActive());
        }
        if (updateUserRequest.getCustomerRank() != null) {
            existingUser.setCustomerRank(updateUserRequest.getCustomerRank());
        }
        if (updateUserRequest.getAccumulatedPoints() != null) {
            existingUser.setAccumulatedPoints(updateUserRequest.getAccumulatedPoints());
        }
        if (updateUserRequest.getCitizenId() != null) {
            existingUser.setCitizenId(updateUserRequest.getCitizenId());
        }
        // Save updated user
        User updatedUser = userService.saveUser(existingUser);
        
        return userMapper.toUserInfoDto(updatedUser);
    }
}
