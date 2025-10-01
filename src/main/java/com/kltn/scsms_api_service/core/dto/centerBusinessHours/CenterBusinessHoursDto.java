package com.kltn.scsms_api_service.core.dto.centerBusinessHours;

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
public class CenterBusinessHoursDto {
    
    private UUID businessHoursId;
    private UUID centerId;
    private String dayOfWeek;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Boolean isClosed;
    private String createdBy;
    private String modifiedBy;
    private java.time.LocalDateTime createdDate;
    private java.time.LocalDateTime modifiedDate;
}
