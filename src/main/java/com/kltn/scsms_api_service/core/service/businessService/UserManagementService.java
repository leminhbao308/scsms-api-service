package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.userManagement.param.UserFilterParam;
import com.kltn.scsms_api_service.core.dto.userManagement.request.CreateUserRequest;
import com.kltn.scsms_api_service.core.dto.userManagement.UserInfoDto;
import com.kltn.scsms_api_service.core.dto.userManagement.request.UpdateUserRequest;
import com.kltn.scsms_api_service.core.entity.Role;
import com.kltn.scsms_api_service.core.entity.User;
import com.kltn.scsms_api_service.core.entity.enumAttribute.CustomerRank;
import com.kltn.scsms_api_service.core.entity.enumAttribute.UserType;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.core.service.entityService.PermissionService;
import com.kltn.scsms_api_service.core.service.entityService.RoleService;
import com.kltn.scsms_api_service.core.service.entityService.UserService;
import com.kltn.scsms_api_service.core.service.entityService.S3FileService;
import com.kltn.scsms_api_service.core.service.websocket.WebSocketService;
import com.kltn.scsms_api_service.core.entity.S3File;
import com.kltn.scsms_api_service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final S3FileService s3FileService;
    private final WebSocketService webSocketService;
    
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
        
        // Determine user type from request or infer from role code
        UserType userType = createUserRequest.getUserType();
        if (userType == null) {
            // Infer user type from role code if not provided
            String roleCode = createUserRequest.getRoleCode();
            if (roleCode != null && roleCode.equals("CUSTOMER")) {
                userType = UserType.CUSTOMER;
            } else {
                // STAFF, ADMIN, MANAGER, etc. are all EMPLOYEE type
                userType = UserType.EMPLOYEE;
            }
        }
        
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
            .userType(userType)
            .isActive(true)
            .build();
        
        // Set customer-specific fields if user is CUSTOMER
        if (userType == UserType.CUSTOMER) {
            newUser.setCustomerRank(createUserRequest.getCustomerRank() != null ? 
                createUserRequest.getCustomerRank() : CustomerRank.BRONZE);
            newUser.setAccumulatedPoints(createUserRequest.getAccumulatedPoints() != null ? 
                createUserRequest.getAccumulatedPoints() : 0);
            newUser.setTotalOrders(createUserRequest.getTotalOrders() != null ? 
                createUserRequest.getTotalOrders() : 0);
            newUser.setTotalSpent(createUserRequest.getTotalSpent() != null ? 
                createUserRequest.getTotalSpent() : 0.0);
        }
        
        // Set employee-specific fields if user is EMPLOYEE
        if (userType == UserType.EMPLOYEE) {
            newUser.setHiredAt(createUserRequest.getHiredAt() != null ? 
                createUserRequest.getHiredAt() : java.time.LocalDateTime.now());
            if (createUserRequest.getCitizenId() != null) {
                newUser.setCitizenId(createUserRequest.getCitizenId());
            }
        }
        
        User createdUser = userService.saveUser(newUser);
        
        log.info("Created new user with email: {}", createdUser.getEmail());
        
        // Notify WebSocket clients about user creation
        try {
            webSocketService.notifyCustomerReload();
            log.info("WebSocket: User creation notification sent");
        } catch (Exception ex) {
            log.error("WebSocket: Failed to send user creation notification: {}", ex.getMessage(), ex);
            // Don't fail the operation if WebSocket notification fails
        }
        
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
            // If current email is same as new email, no need to check uniqueness
            if (!updateUserRequest.getEmail().equals(existingUser.getEmail())) {
                // Check if new email is already in use by another user
                Optional<User> userWithEmail = userService.findByEmail(updateUserRequest.getEmail());
                if (userWithEmail.isPresent() && !userWithEmail.get().getUserId().equals(existingUser.getUserId())) {
                    throw new ClientSideException(ErrorCode.BAD_REQUEST, "Email " + updateUserRequest.getEmail() + " is already in use.");
                }
                existingUser.setEmail(updateUserRequest.getEmail());
            }
        }
        if (updateUserRequest.getFullName() != null) {
            existingUser.setFullName(updateUserRequest.getFullName());
        }
        if (updateUserRequest.getPhoneNumber() != null) {
            // If current phone number is same as new phone number, no need to check uniqueness
            if (!updateUserRequest.getPhoneNumber().equals(existingUser.getPhoneNumber())) {
                // Check if new phone number is already in use by another user
                Optional<User> userWithPhone = userService.findByPhoneNumber(updateUserRequest.getPhoneNumber());
                if (userWithPhone.isPresent() && !userWithPhone.get().getUserId().equals(existingUser.getUserId())) {
                    throw new ClientSideException(ErrorCode.BAD_REQUEST, "Phone number " + updateUserRequest.getPhoneNumber() + " is already in use.");
                }
                existingUser.setPhoneNumber(updateUserRequest.getPhoneNumber());
            }
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
        
        // Notify WebSocket clients about user update
        try {
            webSocketService.notifyCustomerReload();
            log.info("WebSocket: User update notification sent");
        } catch (Exception ex) {
            log.error("WebSocket: Failed to send user update notification: {}", ex.getMessage(), ex);
            // Don't fail the operation if WebSocket notification fails
        }
        
        return userMapper.toUserInfoDto(updatedUser);
    }
    
    public void deleteUser(UUID uuid) {
        // Check user exists
        User existingUser = userService.findById(uuid).orElseThrow(() ->
            new ClientSideException(ErrorCode.NOT_FOUND, "User with ID " + uuid + " not found."));
        
        userService.deleteUser(existingUser);
        
        // Notify WebSocket clients about user deletion
        try {
            webSocketService.notifyCustomerReload();
            log.info("WebSocket: User deletion notification sent");
        } catch (Exception ex) {
            log.error("WebSocket: Failed to send user deletion notification: {}", ex.getMessage(), ex);
            // Don't fail the operation if WebSocket notification fails
        }
    }
    
    public UserInfoDto getUserById(UUID userId) {
        User user = userService.findById(userId).orElseThrow(() ->
            new ClientSideException(ErrorCode.NOT_FOUND, "User with ID " + userId + " not found."));
        
        return userMapper.toUserInfoDto(user);
    }
    
    /**
     * Upload and update user avatar
     */
    @Transactional
    public UserInfoDto uploadUserAvatar(UUID userId, MultipartFile file) {
        log.info("Uploading avatar for user ID: {}", userId);
        
        // Verify user exists
        User user = userService.findById(userId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, 
                "User not found with ID: " + userId));
        
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "File is required");
        }
        
        // Validate file type (only images)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                "Only image files are allowed. Received content type: " + contentType);
        }
        
        // Validate file size (max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                "File size exceeds maximum limit of 5MB. Current size: " + (file.getSize() / 1024) + "KB");
        }
        
        try {
            // Upload file to S3
            S3File s3File = s3FileService.uploadAndSave(
                file,
                "users/" + userId,
                userId, // uploadedBy
                "USER",
                userId
            );
            
            // Update user avatar URL
            user.setAvatarUrl(s3File.getFileUrl());
            User updatedUser = userService.saveUser(user);
            
            log.info("Successfully uploaded avatar for user ID: {}. New avatar URL: {}", 
                userId, s3File.getFileUrl());
            
            // TODO: Optional - Delete old avatar from S3 if exists
            // if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
            //     try {
            //         s3FileService.deleteFileByUrl(oldAvatarUrl);
            //     } catch (Exception e) {
            //         log.warn("Failed to delete old avatar from S3: {}", oldAvatarUrl, e);
            //     }
            // }
            
            // Notify WebSocket clients about avatar upload
            try {
                webSocketService.notifyCustomerReload();
                log.info("WebSocket: Avatar upload notification sent");
            } catch (Exception ex) {
                log.error("WebSocket: Failed to send avatar upload notification: {}", ex.getMessage(), ex);
                // Don't fail the operation if WebSocket notification fails
            }
            
            return userMapper.toUserInfoDto(updatedUser);
            
        } catch (Exception e) {
            log.error("Error uploading avatar for user ID: {}", userId, e);
            throw new ClientSideException(ErrorCode.SYSTEM_ERROR, 
                "Failed to upload avatar: " + e.getMessage());
        }
    }
}
