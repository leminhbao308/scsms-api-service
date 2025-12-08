package com.kltn.scsms_api_service.core.dto.bookingSchedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO cho giờ làm việc
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkingHoursDto {
    
    @JsonProperty("start")
    private LocalTime start;
    
    @JsonProperty("end")
    private LocalTime end;
}

