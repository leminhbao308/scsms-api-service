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
     * Session ID từ frontend (optional nhưng khuyến nghị)
     * Dùng để track conversation và recover draft
     */
    @JsonProperty("session_id")
    private String sessionId;
    
    /**
     * Draft ID (optional)
     * Nếu frontend đã có draft_id từ response trước, gửi lại để backend không phải tìm
     */
    @JsonProperty("draft_id")
    private UUID draftId;
    
    /**
     * Extracted UUIDs từ frontend (optional)
     * Frontend extract UUIDs từ ToolResponse để giảm database queries
     */
    @JsonProperty("extracted_uuids")
    private ExtractedUuids extractedUuids;
    
    /**
     * Chat message trong conversation history
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        @JsonProperty("role")
        private String role; // "user", "assistant", hoặc "tool"
        
        @JsonProperty("content")
        private String content;
        
        /**
         * ToolResponse metadata (nếu message là tool response)
         */
        @JsonProperty("tool_call_id")
        private String toolCallId;
        
        @JsonProperty("tool_name")
        private String toolName;
        
        @JsonProperty("tool_response")
        private Object toolResponse; // ToolResponse data (JSON object)
    }
    
    /**
     * Extracted UUIDs từ ToolResponse
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedUuids {
        @JsonProperty("vehicle_id")
        private String vehicleId;
        
        @JsonProperty("vehicle_license_plate")
        private String vehicleLicensePlate;
        
        @JsonProperty("branch_id")
        private String branchId;
        
        @JsonProperty("branch_name")
        private String branchName;
        
        @JsonProperty("bay_id")
        private String bayId;
        
        @JsonProperty("bay_name")
        private String bayName;
        
        @JsonProperty("service_type")
        private String serviceType;
    }
}

