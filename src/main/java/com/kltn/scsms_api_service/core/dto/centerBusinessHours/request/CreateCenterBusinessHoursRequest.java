package com.kltn.scsms_api_service.core.dto.centerBusinessHours.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCenterBusinessHoursRequest {
    
    @NotNull(message = "Center ID is required")
    private UUID centerId;
    
    @NotBlank(message = "Day of week is required")
    private String dayOfWeek;
    
    private LocalTime openTime;
    
    private LocalTime closeTime;
    
    @Builder.Default
    private Boolean isClosed = false;
}
