package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.core.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.auth.request.LoginRequest;
import com.kltn.scsms_api_service.core.dto.auth.request.RegisterRequest;
import com.kltn.scsms_api_service.core.dto.request.ChangePasswordRequest;
import com.kltn.scsms_api_service.core.dto.request.LogoutRequest;
import com.kltn.scsms_api_service.core.dto.request.RefreshTokenRequest;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.service.businessService.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling authentication operations including Google OAuth2 integration
 * Manages login, logout, token refresh, password operations
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * User login with email and password
     */
    @PostMapping(ApiConstant.LOGIN_API)
    @SwaggerOperation(
        summary = "Perform user login",
        description = "Authenticate user and return access token, refresh token and user info")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest request) {
        
        log.info("Login attempt for email: {}", request.getEmail());
        
        ApiResponse<?> response = authService.login(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Refresh JWT token using refresh token
     */
    @PostMapping(ApiConstant.REFRESH_TOKEN_API)
    @SwaggerOperation(
        summary = "Refresh access token",
        description = "Use refresh token to obtain a new access token")
    public ResponseEntity<ApiResponse<?>> refreshToken(
        @RequestBody RefreshTokenRequest request) {
        
        log.info("Token refresh attempt");
        
        ApiResponse<?> response = authService.refreshToken(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * User logout - invalidate tokens
     */
    @PostMapping(ApiConstant.LOGOUT_API)
    @SwaggerOperation(
        summary = "User logout",
        description = "Invalidate user tokens and logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody LogoutRequest request) {
        
        log.info("Logout request received");
        
        authService.logout(request);
        
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .message("Logout successful")
                .build()
        );
    }
    
    /**
     * Change user password (requires authentication)
     */
    @PostMapping(ApiConstant.CHANGE_PASSWORD_API)
    @SwaggerOperation(
        summary = "Change password",
        description = "Change user password (requires authentication first)")
    public ResponseEntity<ApiResponse<Void>> changePassword(
        @RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {
        
        log.info("Password change request");
        
        authService.changePassword(request, httpRequest);
        
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .message("Password changed successfully")
                .build()
        );
    }
    
    /**
     * Register new user account
     */
    @PostMapping(ApiConstant.REGISTER_API)
    @SwaggerOperation(
        summary = "User registration",
        description = "Register a new user account")
    public ResponseEntity<ApiResponse<?>> register(@RequestBody RegisterRequest request) {
        log.info("User registration attempt for email: {}", request.getEmail());
        
        ApiResponse<?> response = authService.register(request);
        
        return ResponseEntity.ok(response);
    }

//    /**
//     * Get Google OAuth2 authorization URL
//     */
//    @GetMapping("/oauth2/url")
//    @Operation(
//        summary = "Get Google OAuth2 URL",
//        description = "Get Google OAuth2 authorization URL for login"
//    )
//    @ApiResponses(value = {
//        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OAuth2 URL generated successfully")
//    })
//    public ResponseEntity<ApiResponse<GoogleOAuth2UrlResponse>> getGoogleOAuth2Url() {
//
//        log.info("Google OAuth2 URL request");
//
//        GoogleOAuth2UrlResponse urlResponse = authService.getGoogleOAuth2Url();
//
//        return ResponseEntity.ok(
//            ApiResponse.<GoogleOAuth2UrlResponse>builder()
//                .success(true)
//                .message("OAuth2 URL generated successfully")
//                .data(urlResponse)
//                .build()
//        );
//    }
//
//    /**
//     * Google OAuth2 callback handler
//     */
//    @GetMapping("/oauth2/callback")
//    @Operation(
//        summary = "Google OAuth2 callback",
//        description = "Handle Google OAuth2 callback and authenticate user"
//    )
//    @ApiResponses(value = {
//        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "302", description = "Redirect to frontend with tokens"),
//        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "OAuth2 authentication failed")
//    })
//    public void handleGoogleOAuth2Callback(
//        @Parameter(description = "Authorization code from Google")
//        @RequestParam("code") String code,
//        @Parameter(description = "State parameter for CSRF protection")
//        @RequestParam(value = "state", required = false) String state,
//        @Parameter(description = "Error parameter from Google")
//        @RequestParam(value = "error", required = false) String error,
//        HttpServletRequest request,
//        HttpServletResponse response) throws IOException {
//
//        log.info("Google OAuth2 callback received");
//
//        if (error != null) {
//            log.error("OAuth2 error: {}", error);
//            response.sendRedirect("/login?error=" + error);
//            return;
//        }
//
//        try {
//            AuthResponse authResponse = authService.handleGoogleOAuth2Callback(code, state, request);
//
//            // Redirect to frontend with tokens as URL parameters or set as cookies
//            String redirectUrl = String.format("/login-success?access_token=%s&refresh_token=%s",
//                authResponse.getAccessToken(),
//                authResponse.getRefreshToken());
//
//            response.sendRedirect(redirectUrl);
//
//        } catch (Exception e) {
//            log.error("OAuth2 callback error: ", e);
//            response.sendRedirect("/login?error=oauth2_failed");
//        }
//    }
//
//    /**
//     * Get current user information
//     */
//    @GetMapping("/me")
//    @Operation(
//        summary = "Get current user",
//        description = "Get current authenticated user information"
//    )
//    @SecurityRequirement(name = "bearerAuth")
//    @ApiResponses(value = {
//        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User information retrieved"),
//        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
//    })
//    public ResponseEntity<ApiResponse<Object>> getCurrentUser(
//        HttpServletRequest request) {
//
//        Object currentUser = authService.getCurrentUser(request);
//
//        return ResponseEntity.ok(
//            ApiResponse.builder()
//                .success(true)
//                .message("Current user information retrieved")
//                .data(currentUser)
//                .build()
//        );
//    }
//
    
    /**
     * Validate token endpoint
     */
    @PostMapping(ApiConstant.VERIFY_TOKEN_API)
    @SwaggerOperation(
        summary = "Validate token",
        description = "Check if the provided token is valid and not expired")
    public ResponseEntity<ApiResponse<Boolean>> validateToken() {
        
        return ResponseEntity.ok(
            ApiResponse.<Boolean>builder()
                .success(true)
                .message("Token is valid")
                .build()
        );
    }
}
