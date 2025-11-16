package com.kltn.scsms_api_service.core.dto.bookingSchedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO cho available time ranges
 * Chỉ trả về thông tin cần thiết, không expose thông tin booking của khách hàng khác
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableTimeRangesResponse {
    
    @JsonProperty("date")
    private LocalDate date;
    
    @JsonProperty("bay_id")
    private UUID bayId;
    
    @JsonProperty("bay_name")
    private String bayName;
    
    @JsonProperty("working_hours")
    private WorkingHoursDto workingHours;
    
    @JsonProperty("available_time_ranges")
    private List<TimeRangeDto> availableTimeRanges;
}

