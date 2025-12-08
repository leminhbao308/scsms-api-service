package com.kltn.scsms_api_service.core.dto.centerBusinessHours.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCenterBusinessHoursStatusRequest {
    
    @NotNull(message = "Is closed status is required")
    private Boolean isClosed;
}
