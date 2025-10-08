package com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    @JsonProperty("notes")
    private String notes;
    
    @JsonProperty("evidence_media_urls")
    private String evidenceMediaUrls;
}
