package com.kltn.scsms_api_service.core.dto.walkInBooking;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO cho bay recommendation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BayRecommendationRequest {
    
    @JsonProperty("branch_id")
    private UUID branchId;
    
    @JsonProperty("service_type")
    private String serviceType;
    
    @JsonProperty("service_duration_minutes")
    private Integer serviceDurationMinutes;
    
    @JsonProperty("priority")
    private String priority;
}
