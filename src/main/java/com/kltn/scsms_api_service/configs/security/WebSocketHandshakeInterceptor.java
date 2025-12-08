package com.kltn.scsms_api_service.configs.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kltn.scsms_api_service.core.dto.token.LoginUserInfo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket Handshake Interceptor
 * Xử lý JWT authentication trong WebSocket handshake
 * Token được gửi qua query parameter: ws://host:port/ws-native?token=xxx
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    /**
     * Xử lý trước khi handshake
     * Validate JWT token và set authentication vào session attributes
     */
    @Override
    public boolean beforeHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @NonNull Map<String, Object> attributes) throws Exception {

        String uri = request.getURI().getPath();
        log.info("WebSocket: Handshake attempt from {} - URI: {}", request.getRemoteAddress(), uri);
        
        // SockJS info endpoint (/ws/info, /api/ws/info) không phải là WebSocket handshake
        // Nó là HTTP GET request để lấy thông tin server
        // Interceptor này chỉ được gọi cho WebSocket handshake, không phải cho /ws/info
        // Nếu request đến đây với /ws/info, có thể có vấn đề với cấu hình
        // Cho phép info endpoint đi qua mà không cần authentication
        if (uri != null && (uri.endsWith("/info") || uri.contains("/info?"))) {
            log.info("WebSocket: SockJS info endpoint detected (URI: {}), allowing without authentication", uri);
            return true;
        }

        // Extract token từ query parameter
        String token = extractTokenFromRequest(request);

        if (!StringUtils.hasText(token)) {
            log.warn("WebSocket: No token provided in handshake request");
            // Cho phép kết nối không có token (có thể cần cho public endpoints)
            // Nếu muốn require authentication, return false ở đây
            return true;
        }

        try {
            // Validate token
            if (jwtTokenProvider.isTokenExpired(token)) {
                log.error("WebSocket: Token expired");
                return false;
            }

            if (!jwtTokenProvider.validateToken(token, getHttpServletRequest(request))) {
                log.error("WebSocket: Token validation failed");
                return false;
            }

            // Check if token is refresh token (không cho phép dùng refresh token)
            if (jwtTokenProvider.isRefreshToken(token)) {
                log.error("WebSocket: Refresh token used for authentication");
                return false;
            }

            // Check if token is valid (not revoked)
            if (!jwtTokenProvider.isTokenValid(token)) {
                log.error("WebSocket: Token is revoked or not found in database");
                return false;
            }

            // Extract user info từ token
            Claims claims = jwtTokenProvider.getClaimsFromToken(token);
            String json = objectMapper.writeValueAsString(claims);
            LoginUserInfo loginUserInfo = objectMapper.readValue(json, LoginUserInfo.class);

            if (loginUserInfo == null || loginUserInfo.getSub() == null) {
                log.error("WebSocket: Invalid token structure");
                return false;
            }

            // Set authentication vào SecurityContext
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(loginUserInfo, null, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Lưu user info vào session attributes để dùng sau
            attributes.put("user", loginUserInfo);
            attributes.put("userId", loginUserInfo.getSub());
            attributes.put("token", token);

            log.info("WebSocket: Handshake successful for user: {} (email: {})",
                    loginUserInfo.getSub(), loginUserInfo.getEmail());

            return true;

        } catch (Exception ex) {
            log.error("WebSocket: Authentication error during handshake: {}", ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Xử lý sau khi handshake
     * Cleanup nếu cần
     */
    @Override
    public void afterHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            Exception exception) {
        
        if (exception != null) {
            log.error("WebSocket: Handshake failed: {}", exception.getMessage());
        } else {
            log.debug("WebSocket: Handshake completed successfully");
        }
    }

    /**
     * Extract JWT token từ query parameter
     * Format: ws://host:port/ws-native?token=xxx
     */
    private String extractTokenFromRequest(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String token = httpRequest.getParameter("token");
            
            // Nếu không có trong query param, thử lấy từ header (cho STOMP CONNECT frame)
            if (!StringUtils.hasText(token)) {
                // STOMP CONNECT frame có thể gửi token trong header
                // Nhưng trong handshake, chúng ta chỉ có thể lấy từ query param
                log.debug("WebSocket: No token in query parameter");
            }
            
            return token;
        }
        return null;
    }

    /**
     * Convert ServerHttpRequest sang HttpServletRequest để dùng với JwtTokenProvider
     */
    private HttpServletRequest getHttpServletRequest(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            return servletRequest.getServletRequest();
        }
        // Fallback: tạo mock request nếu không phải ServletServerHttpRequest
        // (không nên xảy ra trong Spring Boot)
        return null;
    }
}

