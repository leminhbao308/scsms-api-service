package com.kltn.scsms_api_service.core.dto.bookingSchedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO cho khoảng thời gian trống
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeRangeDto {
    
    @JsonProperty("start_time")
    private LocalTime startTime;
    
    @JsonProperty("end_time")
    private LocalTime endTime;
}

