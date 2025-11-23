package com.kltn.scsms_api_service.core.dto.aiAssistant.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    
    /**
     * Câu trả lời của AI
     * Ví dụ: "Sáng mai có các khung giờ trống: 8:00, 8:30, 9:00..."
     */
    @JsonProperty("message")
    private String message;
    
    /**
     * Danh sách functions đã được gọi (optional)
     * Để frontend có thể hiển thị thông tin debug hoặc UI enhancements
     */
    @JsonProperty("functions_called")
    private List<String> functionsCalled;
    
    /**
     * Có cần user action không
     * Ví dụ: true nếu AI đang chờ user xác nhận đặt lịch
     */
    @JsonProperty("requires_action")
    private Boolean requiresAction;
    
    /**
     * Loại action cần thiết (optional)
     * Ví dụ: "CONFIRM_BOOKING", "SELECT_SLOT", "SELECT_SERVICE"
     */
    @JsonProperty("action_type")
    private String actionType;
}

