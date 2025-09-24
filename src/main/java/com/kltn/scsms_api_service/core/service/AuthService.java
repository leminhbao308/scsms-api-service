package com.kltn.scsms_api_service.core.service;

import com.kltn.scsms_api_service.core.configs.property.JwtTokenProperties;
import com.kltn.scsms_api_service.core.dto.request.ChangePasswordRequest;
import com.kltn.scsms_api_service.core.dto.request.LoginRequest;
import com.kltn.scsms_api_service.core.dto.request.LogoutRequest;
import com.kltn.scsms_api_service.core.dto.request.RefreshTokenRequest;
import com.kltn.scsms_api_service.core.dto.response.AuthResponse;
import com.kltn.scsms_api_service.core.dto.response.RoleResponse;
import com.kltn.scsms_api_service.core.dto.response.UserResponse;
import com.kltn.scsms_api_service.core.entity.TokenType;
import com.kltn.scsms_api_service.core.entity.User;
import com.kltn.scsms_api_service.core.exception.ClientSideException;
import com.kltn.scsms_api_service.core.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProperties jwtTokenProperties;
    
    private final UserService userService;
    private final TokenService tokenService;
    
    public AuthResponse login(@Valid LoginRequest request) {
        User user = userService.findByEmail(request.getEmail())
            .orElseThrow(() -> new ClientSideException(ErrorCode.UNAUTHORIZED, "Invalid email or password"));
        
        if (!user.getIsActive() || user.getIsDeleted())
            throw new ClientSideException(ErrorCode.UNAUTHORIZED, "User account is inactive or deleted");
        
        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // Revoke old tokens and generate new ones
            tokenService.revokeAllUserTokens(user.getUserId());
            
            Map<TokenType, String> tokens = tokenService.generateAndSaveTokens(user);
            
            return buildAuthResponse(tokens, user);
        } else {
            throw new ClientSideException(ErrorCode.UNAUTHORIZED, "Invalid email or password");
        }
    }
    
    public AuthResponse refreshToken(@Valid RefreshTokenRequest request) {
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
        
        Map<TokenType, String> tokens = tokenService.refreshTokens(user);
        
        return buildAuthResponse(tokens, user);
    }
    
    public void logout(LogoutRequest request) {
        // Revoke tokens based on the access token in the Authorization header
        String userId = tokenService.getUserIdFromToken(request.getRefreshToken());
        tokenService.revokeAllUserTokens(java.util.UUID.fromString(userId));
    }
    
    public void changePassword(@Valid ChangePasswordRequest request, HttpServletRequest httpRequest) {
        String token = extractTokenFromHeader(httpRequest.getHeader("Authorization"));
        String userId = tokenService.getUserIdFromToken(token);
        
        String currentPassword = request.getCurrentPassword();
        String newPassword = request.getNewPassword();
        
        User user = userService.findById(java.util.UUID.fromString(userId))
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "User not found"));
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.saveUser(user);
    }
    
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid or empty authorization header");
    }
    
    public boolean validateToken(String token) {
        return tokenService.isValidTokenAndNotExpired(token)
            && tokenService.isTokenExistedAndNotRevoked(token);
    }
    
    private AuthResponse buildAuthResponse(Map<TokenType, String> tokens, User user) {
        RoleResponse roleInfo = RoleResponse.builder()
            .roleId(user.getRole().getRoleId())
            .roleName(user.getRole().getRoleName())
            .roleCode(user.getRole().getRoleCode())
            .description(user.getRole().getDescription())
            .build();
        
        UserResponse userInfo = UserResponse.builder()
            .userId(user.getUserId())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .phoneNumber(user.getPhoneNumber())
            .dateOfBirth(user.getDateOfBirth())
            .address(user.getAddress())
            .avatarUrl(user.getAvatarUrl())
            .dateOfBirth(user.getDateOfBirth())
            .gender(user.getGender())
            .role(roleInfo)
            .build();
        
        return AuthResponse.builder()
            .accessToken(tokens.get(TokenType.ACCESS))
            .refreshToken(tokens.get(TokenType.REFRESH))
            .userInfo(userInfo)
            .build();
    }
}
