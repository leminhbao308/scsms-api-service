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

    /**
     * ToolResponse từ function calls (optional)
     * Chứa dữ liệu từ các function calls (getBranches, getCustomerVehicles, checkAvailability, createBooking)
     * Để frontend có thể extract UUIDs và tránh mất mát dữ liệu
     */
    @JsonProperty("tool_responses")
    private List<ToolResponse> toolResponses;

    /**
     * Draft ID (NEW)
     * ID của booking draft hiện tại
     * Frontend nên gửi lại draft_id trong request tiếp theo để backend không phải tìm
     */
    @JsonProperty("draft_id")
    private java.util.UUID draftId;

    /**
     * Draft data (NEW)
     * Thông tin draft hiện tại để frontend có thể hiển thị progress
     */
    @JsonProperty("draft_data")
    private DraftData draftData;

    /**
     * ToolResponse chứa function name và response data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolResponse {
        @JsonProperty("function_name")
        private String functionName;

        @JsonProperty("response_data")
        private Object responseData; // JSON object chứa response từ function
    }

    /**
     * Draft data để frontend hiển thị progress
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DraftData {
        @JsonProperty("current_step")
        private Integer currentStep;

        @JsonProperty("has_vehicle")
        private Boolean hasVehicle;

        @JsonProperty("has_date")
        private Boolean hasDate;

        @JsonProperty("has_branch")
        private Boolean hasBranch;

        @JsonProperty("has_service")
        private Boolean hasService;

        @JsonProperty("has_bay")
        private Boolean hasBay;

        @JsonProperty("has_time")
        private Boolean hasTime;

        @JsonProperty("is_complete")
        private Boolean isComplete;
    }
}

