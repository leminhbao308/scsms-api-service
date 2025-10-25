package com.kltn.scsms_api_service.core.dto.bookingManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO để tạo booking mới với slot được chọn sẵn
 * Tích hợp tất cả thông tin trong một API call
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingWithSlotRequest {
    
    // Customer information
    @JsonProperty("customer_id")
    private UUID customerId; // nullable nếu là guest
    
    @JsonProperty("customer_name")
    private String customerName;
    
    @JsonProperty("customer_phone")
    private String customerPhone;
    
    @JsonProperty("customer_email")
    private String customerEmail;
    
    // Vehicle information
    @JsonProperty("vehicle_id")
    private UUID vehicleId; // nullable nếu chưa có profile
    
    @JsonProperty("vehicle_license_plate")
    private String vehicleLicensePlate;
    
    @JsonProperty("vehicle_brand_name")
    private String vehicleBrandName;
    
    @JsonProperty("vehicle_model_name")
    private String vehicleModelName;
    
    @JsonProperty("vehicle_type_name")
    private String vehicleTypeName;
    
    @JsonProperty("vehicle_year")
    private Integer vehicleYear;
    
    @JsonProperty("vehicle_color")
    private String vehicleColor;
    
    // Branch information
    @JsonProperty("branch_id")
    private UUID branchId;
    
    // Slot information - REQUIRED for this integrated API
    @JsonProperty("selected_slot")
    private SlotSelectionRequest selectedSlot;
    
    // Service information
    @JsonProperty("booking_items")
    private List<CreateBookingItemRequest> bookingItems;
    
    // Pricing information
    @JsonProperty("total_price")
    private BigDecimal totalPrice;
    
    @Builder.Default
    @JsonProperty("currency")
    private String currency = "VND";
    
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
    
    // Additional information
    @JsonProperty("coupon_code")
    private String couponCode;
    
    @JsonProperty("notes")
    private String notes;
    
    @JsonProperty("special_requests")
    private List<String> specialRequests;
    
    /**
     * Nested DTO for slot selection
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotSelectionRequest {
        
        @JsonProperty("bay_id")
        private UUID bayId;
        
        @JsonProperty("date")
        private LocalDate date;
        
        @JsonProperty("start_time")
        private LocalTime startTime;
        
        @JsonProperty("service_duration_minutes")
        private Integer serviceDurationMinutes;
    }
}
