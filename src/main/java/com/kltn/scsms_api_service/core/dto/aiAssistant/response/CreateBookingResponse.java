package com.kltn.scsms_api_service.core.dto.aiAssistant.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}

