package com.kltn.scsms_api_service.core.dto.walkInBooking;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO cho bay queue stats
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BayQueueStatsResponse {
    
    @JsonProperty("bay_id")
    private UUID bayId;
    
    @JsonProperty("bay_name")
    private String bayName;
    
    @JsonProperty("queue_length")
    private Integer queueLength;
    
    @JsonProperty("estimated_wait_time")
    private Integer estimatedWaitTime;
    
    @JsonProperty("status")
    private String status;
}
