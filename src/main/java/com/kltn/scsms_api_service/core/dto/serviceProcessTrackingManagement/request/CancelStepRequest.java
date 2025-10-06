package com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để hủy bước dịch vụ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelStepRequest {
    
    @NotBlank(message = "Lý do hủy không được để trống")
    private String reason;
}
