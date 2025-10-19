package com.kltn.scsms_api_service.core.dto.bookingManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Request DTO để thay đổi slot của booking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeSlotRequest {
    
    // New slot information
    @JsonProperty("new_bay_id")
    private UUID newBayId;
    
    @JsonProperty("new_slot_date")
    private LocalDate newSlotDate;
    
    @JsonProperty("new_slot_start_time")
    private LocalTime newSlotStartTime;
    
    // Service duration (to calculate how many slots needed)
    @JsonProperty("service_duration_minutes")
    private Integer serviceDurationMinutes;
    
    // Reason for change
    @JsonProperty("reason")
    private String reason;
    
    // Changed by
    @JsonProperty("changed_by")
    private String changedBy;
}
