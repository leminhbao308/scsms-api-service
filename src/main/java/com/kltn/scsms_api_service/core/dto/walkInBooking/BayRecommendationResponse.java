package com.kltn.scsms_api_service.core.dto.walkInBooking;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO cho bay recommendation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BayRecommendationResponse {
    
    @JsonProperty("recommended_bay")
    private BayResponse recommendedBay;
    
    @JsonProperty("queue")
    private List<BookingQueueItemResponse> queue;
    
    @JsonProperty("estimated_wait_time")
    private Integer estimatedWaitTime;
    
    @JsonProperty("reason")
    private String reason;
    
    @JsonProperty("alternative_bays")
    private List<BayResponse> alternativeBays;
}
