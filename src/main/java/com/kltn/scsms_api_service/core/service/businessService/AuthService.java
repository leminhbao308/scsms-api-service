package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.auth.CustomerDto;
import com.kltn.scsms_api_service.core.dto.auth.EmployeeDto;
import com.kltn.scsms_api_service.core.dto.auth.request.LoginRequest;
import com.kltn.scsms_api_service.core.dto.auth.request.RegisterRequest;
import com.kltn.scsms_api_service.core.dto.auth.response.AuthCustomerResponse;
import com.kltn.scsms_api_service.core.dto.auth.response.AuthEmployeeResponse;
import com.kltn.scsms_api_service.core.dto.request.ChangePasswordRequest;
import com.kltn.scsms_api_service.core.dto.request.ForgotPasswordRequest;
import com.kltn.scsms_api_service.core.dto.request.LogoutRequest;
import com.kltn.scsms_api_service.core.dto.request.RefreshTokenRequest;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.entity.Role;
import com.kltn.scsms_api_service.core.entity.User;
import com.kltn.scsms_api_service.core.entity.enumAttribute.CustomerRank;
import com.kltn.scsms_api_service.core.entity.enumAttribute.TokenType;
import com.kltn.scsms_api_service.core.entity.enumAttribute.UserType;
import com.kltn.scsms_api_service.core.service.entityService.RoleService;
import com.kltn.scsms_api_service.core.service.entityService.TokenService;
import com.kltn.scsms_api_service.core.service.entityService.UserService;
import com.kltn.scsms_api_service.core.service.websocket.WebSocketService;
import com.kltn.scsms_api_service.core.entity.Token;
import com.kltn.scsms_api_service.core.dto.auth.response.SessionInfoDto;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final PasswordEncoder passwordEncoder;
    
    private final UserMapper userMapper;
    
    private final UserService userService;
    private final TokenService tokenService;
    private final RoleService roleService;
    private final WebSocketService webSocketService;
    
    public ApiResponse<?> login(@Valid LoginRequest request, HttpServletRequest httpRequest) {
        // Validate that either email or phoneNumber is provided
        if ((request.getEmail() == null || request.getEmail().trim().isEmpty()) &&
            (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Email or phone number is required");
        }
        
        // Find user by email or phone number
        User user = null;
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            user = userService.findByPhoneNumber(request.getPhoneNumber())
                .orElse(null);
        }
        
        // If not found by phone number, try email
        if (user == null && request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            user = userService.findByEmail(request.getEmail())
                .orElse(null);
        }
        
        if (user == null) {
            throw new ClientSideException(ErrorCode.UNAUTHORIZED, "Invalid email/phone number or password");
        }
        
        if (!user.getIsActive() || user.getIsDeleted())
            throw new ClientSideException(ErrorCode.UNAUTHORIZED, "User account is inactive or deleted");
        
        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // Generate device ID if not provided (for backward compatibility)
            String deviceId = request.getDeviceId();
            if (deviceId == null || deviceId.trim().isEmpty()) {
                deviceId = java.util.UUID.randomUUID().toString();
            }
            
            // Extract device info from request
            String deviceName = request.getDeviceName();
            String ipAddress = extractIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            // Generate new tokens WITHOUT revoking old ones (multi-device support)
            Map<TokenType, String> tokens = tokenService.generateAndSaveTokens(
                user, deviceId, deviceName, ipAddress, userAgent);
            
            user.setLastLogin(LocalDateTime.now());
            userService.saveUser(user);
            
            return buildAuthResponse(tokens, user);
        } else {
            throw new ClientSideException(ErrorCode.UNAUTHORIZED, "Invalid email/phone number or password");
        }
    }
    
    public ApiResponse<?> refreshToken(@Valid RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String refreshToken = request.getRefreshToken();
        
        if (!tokenService.isValidTokenAndNotExpired(refreshToken) || !tokenService.isRefreshToken(refreshToken)) {
            throw new ClientSideException(ErrorCode.UNAUTHORIZED, "Invalid or expired refresh token");
        }
        
        if (!tokenService.isTokenExistedAndNotRevoked(refreshToken))
            throw new ClientSideException(ErrorCode.UNAUTHORIZED, "Invalid or expired refresh token");
        
        String userId = tokenService.getUserIdFromToken(refreshToken);
        User user = userService.findById(java.util.UUID.fromString(userId))
            .orElseThrow(() -> new ClientSideException(ErrorCode.UNAUTHORIZED, "Invalid or expired refresh token"));
        
        if (!user.getIsActive() || user.getIsDeleted())
            throw new ClientSideException(ErrorCode.UNAUTHORIZED, "User account is inactive or deleted");
        
        // Extract device info from refresh token claims or request
        String deviceId = tokenService.getDeviceIdFromToken(refreshToken);
        if (deviceId == null || deviceId.trim().isEmpty()) {
            // Fallback: generate new device ID if not in token (for backward compatibility)
            deviceId = java.util.UUID.randomUUID().toString();
        }
        
        // Get device info from token or request
        String deviceName = null;
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        // Refresh tokens for this specific device (doesn't revoke other devices)
        Map<TokenType, String> tokens = tokenService.refreshTokens(
            user, deviceId, deviceName, ipAddress, userAgent);
        
        user.setLastLogin(LocalDateTime.now());
        userService.saveUser(user);
        
        return buildAuthResponse(tokens, user);
    }
    
    public void logout(LogoutRequest request, HttpServletRequest httpRequest) {
        String refreshToken = request.getRefreshToken();
        
        // Get device ID from refresh token
        String deviceId = tokenService.getDeviceIdFromToken(refreshToken);
        String userId = tokenService.getUserIdFromToken(refreshToken);
        
        if (deviceId != null && !deviceId.trim().isEmpty()) {
            // Revoke only tokens for this specific device
            tokenService.revokeTokensByDeviceId(java.util.UUID.fromString(userId), deviceId);
            log.info("Logout: Revoked tokens for userId: {} and deviceId: {}", userId, deviceId);
        } else {
            // Fallback: if no device ID, revoke all tokens (backward compatibility)
            tokenService.revokeAllUserTokens(java.util.UUID.fromString(userId));
            log.info("Logout: Revoked all tokens for userId: {} (no device ID found)", userId);
        }
    }
    
    public void changePassword(@Valid ChangePasswordRequest request, HttpServletRequest httpRequest) {
        String token = extractTokenFromHeader(httpRequest.getHeader("Authorization"));
        String userId = tokenService.getUserIdFromToken(token);
        String currentDeviceId = tokenService.getDeviceIdFromToken(token);
        
        String currentPassword = request.getCurrentPassword();
        String newPassword = request.getNewPassword();
        
        User user = userService.findById(java.util.UUID.fromString(userId))
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "User not found"));
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.saveUser(user);
        
        // Revoke tokens of all OTHER devices (keep current device logged in)
        // This is a security measure: when password changes, other sessions should be invalidated
        if (currentDeviceId != null && !currentDeviceId.trim().isEmpty()) {
            // Get all active ACCESS tokens
            List<Token> activeAccessTokens = tokenService.getActiveTokensByUser(
                user.getUserId(), TokenType.ACCESS);
            
            // Get all active REFRESH tokens
            List<Token> activeRefreshTokens = tokenService.getActiveTokensByUser(
                user.getUserId(), TokenType.REFRESH);
            
            // Revoke all ACCESS tokens except current device
            for (Token tokenEntity : activeAccessTokens) {
                if (tokenEntity.getDeviceId() != null && !tokenEntity.getDeviceId().equals(currentDeviceId)) {
                    tokenService.revokeTokenByValue(tokenEntity.getToken());
                }
            }
            
            // Revoke all REFRESH tokens except current device
            for (Token tokenEntity : activeRefreshTokens) {
                if (tokenEntity.getDeviceId() != null && !tokenEntity.getDeviceId().equals(currentDeviceId)) {
                    tokenService.revokeTokenByValue(tokenEntity.getToken());
                }
            }
            
            log.info("Password changed and tokens revoked for other devices (keeping deviceId: {}) for userId: {}", 
                currentDeviceId, user.getUserId());
        } else {
            // Fallback: if no device ID, revoke all tokens (backward compatibility)
            tokenService.revokeAllUserTokens(user.getUserId());
            log.info("Password changed and all tokens revoked (no device ID) for userId: {}", user.getUserId());
        }
        
        // Notify other devices via WebSocket to logout
        webSocketService.notifyPasswordChanged(userId);
    }
    
    /**
     * Forgot password - reset password using phone number and new password
     */
    public void forgotPassword(@Valid ForgotPasswordRequest request) {
        String phoneNumber = request.getPhoneNumber();
        String newPassword = request.getNewPassword();
        
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Phone number is required");
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "New password is required");
        }
        
        // Find user by phone number
        User user = userService.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "User not found with this phone number"));
        
        if (!user.getIsActive() || user.getIsDeleted()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Account is inactive or deleted");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.saveUser(user);
        
        // Revoke all existing tokens to force logout on all devices
        // This is a security measure: when password is reset, all sessions should be invalidated
        tokenService.revokeAllUserTokens(user.getUserId());
        log.info("Password reset and all tokens revoked for phone number: {}, userId: {}", phoneNumber, user.getUserId());
        
        // Notify all devices via WebSocket to logout
        // This ensures that if user is logged in on other devices, they will be logged out
        webSocketService.notifyPasswordChanged(user.getUserId().toString());
    }
    
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid or empty authorization header");
    }

    private String extractIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // Handle multiple IPs (X-Forwarded-For can contain multiple IPs)
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }
    
    public ApiResponse<?> register(RegisterRequest registerRequest) {
        // Validate email not already in use (only if email is provided)
        if (registerRequest.getEmail() != null && !registerRequest.getEmail().trim().isEmpty()) {
            if (userService.findByEmail(registerRequest.getEmail()).isPresent()) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, "Email " + registerRequest.getEmail() + " is already in use.");
            }
        }
        
        // Always assign CUSTOMER role for new registrations
        Role customerRole = roleService.getRoleByRoleCode("CUSTOMER")
            .orElseThrow(() -> new ClientSideException(ErrorCode.BAD_REQUEST, "Default role CUSTOMER does not exist."));
        
        // Encode password
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        
        // Create new user
        User newUser = userMapper.toEntity(registerRequest);
        
        // Set password (encoded)
        newUser.setPassword(encodedPassword);
        
        // Set role (required)
        newUser.setRole(customerRole);
        
        // Customer specific fields
        newUser.setUserType(UserType.CUSTOMER);
        newUser.setCustomerRank(CustomerRank.BRONZE);
        newUser.setAccumulatedPoints(0);
        newUser.setTotalOrders(0);
        newUser.setTotalSpent(0.0);
        newUser.setIsActive(true);
        
        User createdUser = userService.saveUser(newUser);
        
        Map<TokenType, String> tokens = tokenService.generateAndSaveTokens(createdUser);
        return buildAuthResponse(tokens, createdUser);
    }

    /**
     * Get all active sessions for current user
     */
    public List<SessionInfoDto> getActiveSessions(String accessToken) {
        String userId = tokenService.getUserIdFromToken(accessToken);
        String currentDeviceId = tokenService.getDeviceIdFromToken(accessToken);
        
        List<Token> activeTokens = tokenService.getActiveTokensByUser(
            java.util.UUID.fromString(userId), TokenType.ACCESS);
        
        return activeTokens.stream()
            .map(token -> {
                boolean isCurrentDevice = token.getDeviceId() != null 
                    && token.getDeviceId().equals(currentDeviceId);
                
                return SessionInfoDto.builder()
                    .deviceId(token.getDeviceId())
                    .deviceName(token.getDeviceName())
                    .ipAddress(token.getIpAddress())
                    .userAgent(token.getUserAgent())
                    .createdAt(token.getCreatedAt())
                    .lastActivity(token.getUpdatedAt())
                    .isCurrentDevice(isCurrentDevice)
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * Logout specific device by device ID
     */
    public void logoutDevice(String accessToken, String deviceId) {
        String userId = tokenService.getUserIdFromToken(accessToken);
        tokenService.revokeTokensByDeviceId(java.util.UUID.fromString(userId), deviceId);
        log.info("Logout device: userId={}, deviceId={}", userId, deviceId);
    }

    /**
     * Logout all devices except current device
     */
    public void logoutAllOtherDevices(String accessToken) {
        String userId = tokenService.getUserIdFromToken(accessToken);
        String currentDeviceId = tokenService.getDeviceIdFromToken(accessToken);
        
        if (currentDeviceId == null || currentDeviceId.trim().isEmpty()) {
            // If no device ID, logout all (backward compatibility)
            tokenService.revokeAllUserTokens(java.util.UUID.fromString(userId));
            log.info("Logout all devices: userId={} (no current device ID)", userId);
            return;
        }
        
        // Get all active tokens
        List<Token> activeTokens = tokenService.getActiveTokensByUser(
            java.util.UUID.fromString(userId), TokenType.ACCESS);
        
        // Revoke all tokens except current device
        for (Token token : activeTokens) {
            if (token.getDeviceId() != null && !token.getDeviceId().equals(currentDeviceId)) {
                tokenService.revokeTokenByValue(token.getToken());
            }
        }
        
        log.info("Logout all other devices: userId={}, currentDeviceId={}", userId, currentDeviceId);
    }
    
    private ApiResponse<?> buildAuthResponse(Map<TokenType, String> tokens, User user) {
        if (user.getUserType().equals(UserType.CUSTOMER)) {
            CustomerDto userInfo = userMapper.toCustomerDto(user);
            
            return ApiResponse.success(AuthCustomerResponse.builder()
                .accessToken(tokens.get(TokenType.ACCESS))
                .refreshToken(tokens.get(TokenType.REFRESH))
                .userInfo(userInfo)
                .build());
        } else {
            EmployeeDto userInfo = userMapper.toEmployeeDto(user);
            
            return ApiResponse.success(AuthEmployeeResponse.builder()
                .accessToken(tokens.get(TokenType.ACCESS))
                .refreshToken(tokens.get(TokenType.REFRESH))
                .userInfo(userInfo)
                .build());
        }
    }
}
