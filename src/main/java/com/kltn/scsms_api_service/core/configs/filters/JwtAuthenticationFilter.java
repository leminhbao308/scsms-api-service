package com.kltn.scsms_api_service.core.configs.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kltn.scsms_api_service.core.configs.security.JwtTokenProvider;
import com.kltn.scsms_api_service.core.constants.ApiConstant;
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
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    @Override
    protected final void doFilterInternal(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final FilterChain filterChain)
        throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Skip authentication for non-protected paths
        if (isBypassPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String token = jwtTokenProvider.extractJwtFromRequest(request);
            
            if (StringUtils.hasText(token)) {
                // Validate token format and signature
                if (!jwtTokenProvider.validateToken(token, request)) {
                    log.error(
                        "AuthFilter - Token validation failed for request: {}", request.getRequestURI());
                    writeErrorResponse(response, "Unauthorized - Invalid token");
                    return;
                }
                
                // Check if token is access token (not refresh token)
                if (jwtTokenProvider.isRefreshToken(token)) {
                    log.error(
                        "AuthFilter - Refresh token used for authentication: {}", request.getRequestURI());
                    writeErrorResponse(response, "Unauthorizes - Invalid token type");
                    return;
                }
                
                // Check if token exists in database and is not revoked
                if (!jwtTokenProvider.isTokenValid(token)) {
                    log.error(
                        "AuthFilter - Token is revoked or not found in database: {}",
                        request.getRequestURI());
                    writeErrorResponse(response, "Unauthorized - Token is revoked or expired");
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
                } else {
                    log.error("AuthFilter - Invalid token structure: {}", request.getRequestURI());
                    writeErrorResponse(response, "Unauthorized - Invalid token structure");
                    return;
                }
            } else {
                log.error("AuthFilter - Missing JWT Token for protected path: {}", request.getRequestURI());
                writeErrorResponse(response, "Unauthorized - Missing JWT Token");
                return;
            }
        } catch (Exception ex) {
            log.error(
                "AuthFilter - Authentication error for path {}: {}",
                request.getRequestURI(),
                ex.getMessage());
            writeErrorResponse(response, "Unauthorized - Authentication failed");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isBypassPath(String path) {
        return path.contains("/actuator")
            || path.contains("/docs/api-docs")
            || path.contains("/docs/swagger-ui")
            || path.contains("/swagger-resources")
            || path.contains("/webjars")
            || path.contains("/swagger-ui.html")
            || path.equals(API_PREFIX + ApiConstant.LOGIN_API) // Allow login
            || path.equals(API_PREFIX + ApiConstant.REFRESH_TOKEN_API); // Allow refresh
    }
    
    private void writeErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ResponseEntity<?> res = ResponseBuilder.unauthorized(message);
        response.getWriter().write(objectMapper.writeValueAsString(res));
    }
}
