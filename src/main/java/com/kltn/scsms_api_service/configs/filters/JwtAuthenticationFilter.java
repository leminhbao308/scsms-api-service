package com.kltn.scsms_api_service.configs.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kltn.scsms_api_service.configs.security.JwtTokenProvider;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.token.LoginUserInfo;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    
    private static String API_PREFIX = "/api";
    
    private static final List<String> PROTECTED_PATH_PATTERNS = ApiConstant.PROTECTED_PATHS(API_PREFIX);
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    @Override
    protected final void doFilterInternal(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final FilterChain filterChain)
        throws ServletException, IOException {
        
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // Log ALL requests to debug WebSocket info endpoint issue
        log.debug("AuthFilter - Processing path: {} (method: {})", path, method);
        
        boolean isProtected =
            PROTECTED_PATH_PATTERNS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
        
        // Skip bypass paths (actuator, swagger, login, etc.)
        if (isBypassPath(path, method)) {
            log.info("AuthFilter - Bypassing path: {} (method: {})", path, method);
            filterChain.doFilter(request, response);
            return;
        }
        
        // Extract token từ request
        String token = jwtTokenProvider.extractJwtFromRequest(request);
        
        // Nếu có token, validate và set authentication (bất kể protected hay không)
        if (StringUtils.hasText(token)) {
            try {
                // Validate token format and signature
                if (jwtTokenProvider.isTokenExpired(token) || !jwtTokenProvider.validateToken(token, request)) {
                    log.error("AuthFilter - Token validation failed for request: {}", request.getRequestURI());
                    
                    // CHỈ reject nếu là protected path
                    if (isProtected) {
                        writeErrorResponse(response, "Unauthorized - Invalid token");
                        return;
                    }
                    // Nếu không phải protected path, bỏ qua lỗi và tiếp tục
                    log.warn("AuthFilter - Invalid token for non-protected path, continuing without authentication");
                    filterChain.doFilter(request, response);
                    return;
                }
                
                // Check if token is access token (not refresh token)
                if (jwtTokenProvider.isRefreshToken(token)) {
                    log.error("AuthFilter - Refresh token used for authentication: {}", request.getRequestURI());
                    
                    if (isProtected) {
                        writeErrorResponse(response, "Unauthorized - Invalid token type");
                        return;
                    }
                    log.warn("AuthFilter - Refresh token for non-protected path, continuing without authentication");
                    filterChain.doFilter(request, response);
                    return;
                }
                
                // Check if token exists in database and is not revoked
                if (!jwtTokenProvider.isTokenValid(token)) {
                    log.error("AuthFilter - Token is revoked or not found in database: {}", request.getRequestURI());
                    
                    if (isProtected) {
                        writeErrorResponse(response, "Unauthorized - Token is revoked or expired");
                        return;
                    }
                    log.warn("AuthFilter - Revoked token for non-protected path, continuing without authentication");
                    filterChain.doFilter(request, response);
                    return;
                }
                
                // Extract claims and create authentication
                Claims claims = jwtTokenProvider.getClaimsFromToken(token);
                String json = objectMapper.writeValueAsString(claims);
                LoginUserInfo loginUserInfo = objectMapper.readValue(json, LoginUserInfo.class);
                
                if (loginUserInfo != null && loginUserInfo.getSub() != null) {
                    MDC.put("sub", loginUserInfo.getSub());
                    
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(loginUserInfo, null, null);
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.info("AuthFilter - Authenticated user: {} (email: {}) for path: {}",
                        loginUserInfo.getSub(), loginUserInfo.getEmail(), request.getRequestURI());
                } else {
                    log.error("AuthFilter - Invalid token structure: {}", request.getRequestURI());
                    
                    if (isProtected) {
                        writeErrorResponse(response, "Unauthorized - Invalid token structure");
                        return;
                    }
                }
            } catch (Exception ex) {
                log.error("AuthFilter - Authentication error for path {}: {}", request.getRequestURI(), ex.getMessage());
                
                if (isProtected) {
                    writeErrorResponse(response, "Unauthorized - Authentication failed");
                    return;
                }
                // Nếu không phải protected path, log error nhưng vẫn tiếp tục
                log.warn("AuthFilter - Authentication failed for non-protected path, continuing without authentication");
            }
        } else {
            // Không có token
            if (isProtected) {
                log.error("AuthFilter - Missing JWT Token for protected path: {}", request.getRequestURI());
                writeErrorResponse(response, "Unauthorized - Missing JWT Token");
                return;
            }
            // Nếu không phải protected path và không có token, tiếp tục bình thường
            // Let AnonymousAuthenticationFilter handle anonymous authentication for permitAll() to work
            log.debug("AuthFilter - No token provided for non-protected path: {}", request.getRequestURI());
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isBypassPath(String path, String method) {
        boolean bypass = path.contains("/actuator")
            || path.contains("/docs/api-docs")
            || path.contains("/docs/swagger-ui")
            || path.contains("/swagger-resources")
            || path.contains("/webjars")
            || path.contains("/swagger-ui.html")
            || path.startsWith("/ws/") // Allow WebSocket endpoints (SockJS info endpoint)
            || path.startsWith("/ws-native/") // Allow native WebSocket endpoints
            || path.startsWith("/api/ws/") // Allow WebSocket endpoints with context-path
            || path.startsWith("/api/ws-native/") // Allow native WebSocket endpoints with context-path
            || path.equals(API_PREFIX + ApiConstant.LOGIN_API) // Allow login
            || path.equals(API_PREFIX + ApiConstant.REFRESH_TOKEN_API) // Allow refresh
            || path.equals(API_PREFIX + ApiConstant.REGISTER_API) // Allow registration
            || path.equals(API_PREFIX + ApiConstant.LOGOUT_API) // Allow logout
            || path.startsWith(API_PREFIX + "/otp/"); // Allow OTP endpoints
        
        // Public GET endpoints - allow Guest access (only GET methods)
        if (!bypass && "GET".equals(method)) {
            bypass = path.startsWith(API_PREFIX + "/products/") // Products GET endpoints
                || path.startsWith(API_PREFIX + "/services/") // Services GET endpoints
                || path.equals(API_PREFIX + "/service-types/dropdown") // Service types dropdown
                || path.equals(API_PREFIX + "/service-types/active") // Service types active
                || path.equals(API_PREFIX + "/centers/get-all") // Centers get-all
                || path.startsWith(API_PREFIX + "/branches/") // Branches GET endpoints
                || path.startsWith(API_PREFIX + "/media/entity/"); // Media GET endpoints
        }
        
        // Public POST endpoints - allow Guest access for pricing preview (POST because they have request body)
        if (!bypass && "POST".equals(method)) {
            bypass = path.equals(API_PREFIX + "/pricing/preview") // Pricing preview
                || path.equals(API_PREFIX + "/pricing/preview-batch") // Batch pricing preview
                || path.equals(API_PREFIX + "/pricing/batch-service-prices"); // Batch service prices
        }
        
        log.debug("AuthFilter - Checking bypass for path {} (method: {}): {}", path, method, bypass);
        return bypass;
    }
    
    private void writeErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ResponseEntity<?> res = ResponseBuilder.unauthorized(message);
        response.getWriter().write(objectMapper.writeValueAsString(res));
    }
}
