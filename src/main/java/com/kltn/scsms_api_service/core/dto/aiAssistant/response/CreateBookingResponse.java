package com.kltn.scsms_api_service.core.dto.aiAssistant.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingResponse {
    
    /**
     * Trạng thái: "SUCCESS" hoặc "FAILED"
     */
    @JsonProperty("status")
    private String status;
    
    /**
     * Mã booking (ví dụ: "BK-20251121-0001")
     */
    @JsonProperty("booking_code")
    private String bookingCode;
    
    /**
     * Thông báo cho AI
     * AI sẽ dùng để trả lời user
     */
    @JsonProperty("message")
    private String message;
    
    /**
     * Chi tiết booking (optional)
     * Để AI cung cấp thông tin đầy đủ cho user
     */
    @JsonProperty("booking_details")
    private BookingDetails bookingDetails;
    
    /**
     * State tracking - Bước hiện tại trong quy trình đặt lịch
     * Giúp AI biết đang ở bước nào và cần làm gì tiếp theo
     */
    @JsonProperty("state")
    private BookingState state;
    
    /**
     * Bước bị lỗi (nếu có)
     */
    @JsonProperty("failed_step")
    private Integer failedStep;
    
    /**
     * Chi tiết booking
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingDetails {
        @JsonProperty("date_time")
        private String dateTime;
        
        @JsonProperty("service_name")
        private String serviceName;
        
        @JsonProperty("duration")
        private Integer duration;
        
        @JsonProperty("branch_name")
        private String branchName;
        
        @JsonProperty("bay_name")
        private String bayName;
        
        @JsonProperty("total_price")
        private Long totalPrice;
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
         * Bước hiện tại (1-7)
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
         * Dữ liệu còn thiếu
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

