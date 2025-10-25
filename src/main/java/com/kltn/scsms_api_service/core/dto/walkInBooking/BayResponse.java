package com.kltn.scsms_api_service.core.dto.walkInBooking;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO cho bay information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BayResponse {
    
    @JsonProperty("bay_id")
    private UUID bayId;
    
    @JsonProperty("bay_name")
    private String bayName;
    
    @JsonProperty("bay_code")
    private String bayCode;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("allow_booking")
    private Boolean allowBooking;
}
