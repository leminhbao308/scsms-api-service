package com.kltn.scsms_api_service.core.dto.walkInBooking;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO cho walk-in booking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalkInBookingResponse {
    
    @JsonProperty("booking_id")
    private UUID bookingId;
    
    @JsonProperty("booking_code")
    private String bookingCode;
    
    @JsonProperty("assigned_bay_id")
    private UUID assignedBayId;
    
    @JsonProperty("queue_position")
    private Integer queuePosition;
    
    @JsonProperty("estimated_start_time")
    private LocalDateTime estimatedStartTime;
    
    @JsonProperty("estimated_wait_time")
    private Integer estimatedWaitTime;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("message")
    private String message;
}
