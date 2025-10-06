package com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để hoàn thành bước dịch vụ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteStepRequest {
    
    private String notes;
    
    private String evidenceMediaUrls;
}
