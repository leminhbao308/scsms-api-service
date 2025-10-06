package com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO để bắt đầu thực hiện bước dịch vụ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartStepRequest {
    
    @NotNull(message = "Technician ID không được để trống")
    private UUID technicianId;
    
    private String notes;
}
