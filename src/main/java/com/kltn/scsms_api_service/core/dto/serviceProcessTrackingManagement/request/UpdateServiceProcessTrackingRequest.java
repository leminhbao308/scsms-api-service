package com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.ServiceProcessTracking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để cập nhật tracking quá trình thực hiện dịch vụ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceProcessTrackingRequest {
    
    @JsonProperty("status")
    private ServiceProcessTracking.TrackingStatus status;
    
    // Removed: estimated_duration - simplified tracking
    // Removed: progress_percent - simplified tracking
    
    @JsonProperty("notes")
    private String notes;
    
    @JsonProperty("evidence_media_urls")
    private String evidenceMediaUrls;
}
