package com.kltn.scsms_api_service.core.dto.serviceBayManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO chứa thông tin tính khả dụng của service bay
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBayAvailabilityDto {
    
    @JsonProperty("bay_id")
    private UUID bayId;
    
    @JsonProperty("bay_name")
    private String bayName;
    
    @JsonProperty("bay_code")
    private String bayCode;
    
    @JsonProperty("bay_type")
    private String bayType;
    
    @JsonProperty("is_available")
    private Boolean isAvailable;
    
    @JsonProperty("start_time")
    private LocalDateTime startTime;
    
    @JsonProperty("end_time")
    private LocalDateTime endTime;
    
    @JsonProperty("conflicts")
    private List<BookingConflictDto> conflicts;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingConflictDto {
        @JsonProperty("booking_id")
        private UUID bookingId;
        
        @JsonProperty("booking_code")
        private String bookingCode;
        
        @JsonProperty("customer_name")
        private String customerName;
        
        @JsonProperty("scheduled_start_at")
        private LocalDateTime scheduledStartAt;
        
        @JsonProperty("scheduled_end_at")
        private LocalDateTime scheduledEndAt;
        
        @JsonProperty("status")
        private String status;
    }
}
