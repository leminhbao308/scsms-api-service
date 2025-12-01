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
     * State tracking - Bước hiện tại trong quy trình đặt lịch
     * Giúp AI biết đang ở bước nào và cần làm gì tiếp theo
     * Format: {"current_step": 3, "required_data": ["branch_id"], "missing_data": ["branch_id"]}
     */
    @JsonProperty("state")
    private BookingState state;
    
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
    
    /**
     * State tracking cho quy trình đặt lịch
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingState {
        /**
         * Bước hiện tại (1-7):
         * 1: Chọn xe
         * 2: Chọn ngày
         * 3: Chọn chi nhánh
         * 4: Chọn dịch vụ
         * 5: Chọn bay
         * 6: Chọn giờ
         * 7: Xác nhận và tạo booking
         */
        @JsonProperty("current_step")
        private Integer currentStep;
        
        /**
         * Dữ liệu đã có
         */
        @JsonProperty("has_vehicle_id")
        private Boolean hasVehicleId;
        
        @JsonProperty("has_date_time")
        private Boolean hasDateTime;
        
        @JsonProperty("has_branch_id")
        private Boolean hasBranchId;
        
        @JsonProperty("has_service_type")
        private Boolean hasServiceType;
        
        @JsonProperty("has_bay_id")
        private Boolean hasBayId;
        
        @JsonProperty("has_time_slot")
        private Boolean hasTimeSlot;
        
        /**
         * Dữ liệu còn thiếu cho bước hiện tại
         */
        @JsonProperty("missing_data")
        private List<String> missingData;
        
        /**
         * Thông báo cho AI về bước tiếp theo cần làm
         */
        @JsonProperty("next_action")
        private String nextAction;
    }
}

