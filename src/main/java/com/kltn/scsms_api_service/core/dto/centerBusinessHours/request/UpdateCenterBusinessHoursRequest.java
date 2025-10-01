package com.kltn.scsms_api_service.core.dto.centerBusinessHours.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCenterBusinessHoursRequest {
    
    private String dayOfWeek;
    
    private LocalTime openTime;
    
    private LocalTime closeTime;
    
    private Boolean isClosed;
}
