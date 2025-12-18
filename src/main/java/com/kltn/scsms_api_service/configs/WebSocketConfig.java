package com.kltn.scsms_api_service.configs;

import com.kltn.scsms_api_service.configs.security.WebSocketHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketHandshakeInterceptor webSocketHandshakeInterceptor;

    @Value("${app.cors.allowed-origin-patterns}")
    private String allowedOriginPatterns;
    
    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    /**
     * Cấu hình STOMP message broker
     * - Enable simple broker cho các topics
     * - Set application destination prefix cho client send messages
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory message broker để broadcast messages đến clients
        // Prefix "/topic" cho broadcast messages (pub/sub pattern)
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefix "/app" cho messages từ client gửi đến server
        // (Hiện tại chưa cần, nhưng có thể dùng sau)
        config.setApplicationDestinationPrefixes("/app");
        
        log.info("WebSocket: Message broker configured - topics: /topic, /queue, app prefix: /app");
    }

    /**
     * Đăng ký STOMP endpoints
     * - /ws: SockJS endpoint (cho Web browsers)
     * - /ws-native: Native WebSocket endpoint (cho Mobile apps)
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Parse allowed origins từ config
        List<String> origins = Arrays.stream(allowedOriginPatterns.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        log.info("WebSocket: Registering endpoints with allowed origins: {}", origins);
        log.info("WebSocket: Context path: {}", contextPath);

        // Endpoint cho Web (SockJS)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(origins.toArray(new String[0]))
                .addInterceptors(webSocketHandshakeInterceptor)
                .withSockJS()
                .setHeartbeatTime(25000)
                .setDisconnectDelay(5000);
        
                // Endpoint cho Web (SockJS) với context-path
        if (contextPath != null && !contextPath.equals("/") && !contextPath.isEmpty()) {
            String wsPath = contextPath + "/ws";
            registry.addEndpoint(wsPath)
                    .setAllowedOriginPatterns(origins.toArray(new String[0]))
                    .addInterceptors(webSocketHandshakeInterceptor)
                    .withSockJS()
                    .setHeartbeatTime(25000)
                    .setDisconnectDelay(5000);
            log.info("WebSocket: Also registered endpoint with context-path: {}", wsPath);
        }

        // Endpoint cho Mobile (Native WebSocket)
        // QUAN TRỌNG: Mobile apps (React Native) không có HTTP origin như web browsers
        // Cho phép tất cả origins vì authentication được thực hiện qua JWT token trong query parameter
        // Token được validate trong WebSocketHandshakeInterceptor, nên vẫn đảm bảo security
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*")  // Cho phép tất cả origins cho mobile apps
                .addInterceptors(webSocketHandshakeInterceptor);
        
        // Endpoint cho Mobile (Native WebSocket) với context-path
        if (contextPath != null && !contextPath.equals("/") && !contextPath.isEmpty()) {
            String wsNativePath = contextPath + "/ws-native";
            registry.addEndpoint(wsNativePath)
                    .setAllowedOriginPatterns("*")  // Cho phép tất cả origins cho mobile apps
                    .addInterceptors(webSocketHandshakeInterceptor);
            log.info("WebSocket: Also registered endpoint with context-path: {}", wsNativePath);
        }

        log.info("WebSocket: Endpoints registered - /ws (SockJS), /ws-native (Native)");
    }
}

