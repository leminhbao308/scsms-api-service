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
 * Request DTO để thay đổi schedule (bay, date, time) của booking
 * Không còn tạo slot entity, chỉ update scheduledStartAt/scheduledEndAt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeScheduleRequest {
    
    // New scheduling information
    @JsonProperty("new_bay_id")
    private UUID newBayId;
    
    @JsonProperty("new_schedule_date")
    private LocalDate newScheduleDate;
    
    @JsonProperty("new_schedule_start_time")
    private LocalTime newScheduleStartTime;
    
    // Service duration (to calculate scheduledEndAt)
    @JsonProperty("service_duration_minutes")
    private Integer serviceDurationMinutes;
    
    // Reason for change
    @JsonProperty("reason")
    private String reason;
    
    // Changed by
    @JsonProperty("changed_by")
    private String changedBy;
}

