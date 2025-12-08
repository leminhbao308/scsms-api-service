package com.kltn.scsms_api_service.core.dto.walkInBooking;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO cho transfer booking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferBookingRequest {
    
    @JsonProperty("booking_id")
    private UUID bookingId;
    
    @JsonProperty("from_bay_id")
    private UUID fromBayId;
    
    @JsonProperty("to_bay_id")
    private UUID toBayId;
    
    @JsonProperty("reason")
    private String reason;
}
