package com.kltn.scsms_api_service.core.dto.aiAssistant.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    
    /**
     * Câu hỏi/yêu cầu của user
     * Ví dụ: "Tôi muốn đặt lịch rửa xe sáng mai"
     */
    @JsonProperty("message")
    private String message;
    
    /**
     * Lịch sử hội thoại (optional)
     * Để AI có context về cuộc hội thoại trước đó
     */
    @JsonProperty("conversation_history")
    private List<ChatMessage> conversationHistory;
    
    /**
     * Số điện thoại user (optional)
     * Để xác định customer nếu user chưa đăng nhập
     */
    @JsonProperty("customer_phone")
    private String customerPhone;
    
    /**
     * Customer ID (optional)
     * Nếu user đã đăng nhập, có thể truyền customerId
     */
    @JsonProperty("customer_id")
    private UUID customerId;
    
    /**
     * Chat message trong conversation history
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        @JsonProperty("role")
        private String role; // "user" hoặc "assistant"
        
        @JsonProperty("content")
        private String content;
    }
}

