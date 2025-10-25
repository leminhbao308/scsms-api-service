package com.kltn.scsms_api_service.core.dto.walkInBooking;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO cho walk-in booking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalkInBookingRequest {
    
    @JsonProperty("customer_type")
    private String customerType; // EXISTING, NEW
    
    // Cho khách hàng có sẵn
    @JsonProperty("customer_id")
    private UUID customerId;
    
    @JsonProperty("vehicle_id")
    private UUID vehicleId;
    
    // Cho khách hàng mới
    @JsonProperty("customer_name")
    private String customerName;
    
    @JsonProperty("customer_phone")
    private String customerPhone;
    
    @JsonProperty("customer_email")
    private String customerEmail;
    
    @JsonProperty("vehicle_license_plate")
    private String vehicleLicensePlate;
    
    @JsonProperty("vehicle_brand")
    private String vehicleBrand;
    
    @JsonProperty("vehicle_model")
    private String vehicleModel;
    
    @JsonProperty("vehicle_type")
    private String vehicleType;
    
    @JsonProperty("vehicle_color")
    private String vehicleColor;
    
    @JsonProperty("vehicle_year")
    private Integer vehicleYear;
    
    // Thông tin chung
    @JsonProperty("assigned_bay_id")
    private UUID assignedBayId;
    
    @JsonProperty("branch_id")
    private UUID branchId;
    
    @JsonProperty("services")
    private List<ServiceRequest> services;
    
    @JsonProperty("total_price")
    private BigDecimal totalPrice;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("deposit_amount")
    private BigDecimal depositAmount;
    
    // Scheduling information
    @JsonProperty("estimated_duration_minutes")
    private Integer estimatedDurationMinutes;
    
    @JsonProperty("preferred_start_at")
    private String preferredStartAt; // ISO string format
    
    @JsonProperty("scheduled_start_at")
    private String scheduledStartAt; // ISO string format
    
    @JsonProperty("scheduled_end_at")
    private String scheduledEndAt; // ISO string format
    
    @JsonProperty("slot_start_time")
    private String slotStartTime; // HH:mm format
    
    @JsonProperty("slot_end_time")
    private String slotEndTime; // HH:mm format
    
    @JsonProperty("notes")
    private String notes;
    
    @JsonProperty("priority")
    private String priority;
    
    @JsonProperty("special_requests")
    private List<String> specialRequests;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceRequest {
        @JsonProperty("service_id")
        private UUID serviceId;
        
        @JsonProperty("service_name")
        private String serviceName;
        
        @JsonProperty("duration_minutes")
        private Integer durationMinutes;
        
        @JsonProperty("price")
        private BigDecimal price;
    }
}
