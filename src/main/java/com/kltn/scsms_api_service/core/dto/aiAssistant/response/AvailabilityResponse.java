package com.kltn.scsms_api_service.core.dto.aiAssistant.response;

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
public class AvailabilityResponse {
    
    /**
     * Trạng thái: "AVAILABLE" hoặc "FULL"
     */
    @JsonProperty("status")
    private String status;
    
    /**
     * Giờ trống cụ thể (nếu user hỏi giờ cụ thể)
     * Format: "HH:mm" (ví dụ: "08:00", "14:30")
     */
    @JsonProperty("slot")
    private String slot;
    
    /**
     * Danh sách các giờ gợi ý
     * Format: ["08:00", "08:30", "09:00", ...]
     * AI sẽ dùng để suggest cho user
     */
    @JsonProperty("suggestions")
    private List<String> suggestions;
    
    /**
     * Danh sách các bay có slot trống
     * Nếu có nhiều bay, AI sẽ suggest bay phù hợp nhất
     */
    @JsonProperty("available_bays")
    private List<AvailableBayInfo> availableBays;
    
    /**
     * Thông báo cho AI
     * AI sẽ dùng để trả lời user
     */
    @JsonProperty("message")
    private String message;
    
    /**
     * Danh sách dịch vụ gợi ý (khi serviceType không rõ ràng)
     * Format: [{"service_id": "...", "service_name": "Rửa xe cơ bản", "description": "..."}, ...]
     * AI sẽ dùng để yêu cầu user chọn dịch vụ cụ thể
     */
    @JsonProperty("suggested_services")
    private List<SuggestedServiceInfo> suggestedServices;
    
    /**
     * Thông tin bay có slot trống
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailableBayInfo {
        @JsonProperty("bay_id")
        private UUID bayId;
        
        @JsonProperty("bay_name")
        private String bayName;
        
        @JsonProperty("bay_code")
        private String bayCode;
        
        @JsonProperty("branch_id")
        private UUID branchId;
        
        @JsonProperty("branch_name")
        private String branchName;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("available_slots")
        private List<String> availableSlots;
    }
    
    /**
     * Thông tin dịch vụ gợi ý
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuggestedServiceInfo {
        @JsonProperty("service_id")
        private UUID serviceId;
        
        @JsonProperty("service_name")
        private String serviceName;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("estimated_duration")
        private Integer estimatedDuration;
        
        @JsonProperty("price")
        private java.math.BigDecimal price;
    }
}

