package com.kltn.scsms_api_service.core.dto.walkInBooking;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO cho booking queue item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingQueueItemResponse {
    
    @JsonProperty("booking_id")
    private UUID bookingId;
    
    @JsonProperty("booking_code")
    private String bookingCode;
    
    @JsonProperty("customer_name")
    private String customerName;
    
    @JsonProperty("customer_phone")
    private String customerPhone;
    
    @JsonProperty("vehicle_license_plate")
    private String vehicleLicensePlate;
    
    @JsonProperty("service_type")
    private String serviceType;
    
    @JsonProperty("queue_position")
    private Integer queuePosition;
    
    @JsonProperty("estimated_start_time")
    private LocalDateTime estimatedStartTime;
    
    @JsonProperty("estimated_completion_time")
    private LocalDateTime estimatedCompletionTime;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("booking_service_names")
    private java.util.List<String> bookingServiceNames;
    
    @JsonProperty("booking_total_price")
    private java.math.BigDecimal bookingTotalPrice;
    
    @JsonProperty("booking_customer_name")
    private String bookingCustomerName;
    
    @JsonProperty("booking_vehicle_license_plate")
    private String bookingVehicleLicensePlate;
}
