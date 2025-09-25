package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.auth.CustomerDto;
import com.kltn.scsms_api_service.core.dto.auth.EmployeeDto;
import com.kltn.scsms_api_service.core.dto.auth.request.LoginRequest;
import com.kltn.scsms_api_service.core.dto.auth.request.RegisterRequest;
import com.kltn.scsms_api_service.core.dto.auth.response.AuthCustomerResponse;
import com.kltn.scsms_api_service.core.dto.auth.response.AuthEmployeeResponse;
import com.kltn.scsms_api_service.core.dto.request.ChangePasswordRequest;
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
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final PasswordEncoder passwordEncoder;
    
    private final UserMapper userMapper;
    
    private final UserService userService;
    private final TokenService tokenService;
    private final RoleService roleService;
    
    public ApiResponse<?> login(@Valid LoginRequest request) {
        User user = userService.findByEmail(request.getEmail())
            .orElseThrow(() -> new ClientSideException(ErrorCode.UNAUTHORIZED, "Invalid email or password"));
        
        if (!user.getIsActive() || user.getIsDeleted())
            throw new ClientSideException(ErrorCode.UNAUTHORIZED, "User account is inactive or deleted");
        
        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // Revoke old tokens and generate new ones
            tokenService.revokeAllUserTokens(user.getUserId());
            
            Map<TokenType, String> tokens = tokenService.generateAndSaveTokens(user);
            
            user.setLastLogin(LocalDateTime.now());
            userService.saveUser(user);
            
            return buildAuthResponse(tokens, user);
        } else {
            throw new ClientSideException(ErrorCode.UNAUTHORIZED, "Invalid email or password");
        }
    }
    
    public ApiResponse<?> refreshToken(@Valid RefreshTokenRequest request) {
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
        
        user.setLastLogin(LocalDateTime.now());
        userService.saveUser(user);
        
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
    
    public ApiResponse<?> register(RegisterRequest registerRequest) {
        // Validate email not already in use
        if (userService.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Email " + registerRequest.getEmail() + " is already in use.");
        }
        
        // Always assign CUSTOMER role for new registrations
        Role customerRole = roleService.getRoleByRoleCode("CUSTOMER")
            .orElseThrow(() -> new ClientSideException(ErrorCode.BAD_REQUEST, "Default role CUSTOMER does not exist."));
        
        // Encode password
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        
        // Create new user
        User newUser = userMapper.toEntity(registerRequest);
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
