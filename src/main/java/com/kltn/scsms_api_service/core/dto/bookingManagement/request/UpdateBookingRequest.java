package com.kltn.scsms_api_service.core.dto.bookingManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO để cập nhật booking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingRequest {
    
    // Customer information
    @JsonProperty("customer_name")
    private String customerName;
    @JsonProperty("customer_phone")
    private String customerPhone;
    @JsonProperty("customer_email")
    private String customerEmail;
    
    // Vehicle information
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
    
    // Scheduling information
    @JsonProperty("preferred_start_at")
    private LocalDateTime preferredStartAt;
    @JsonProperty("scheduled_start_at")
    private LocalDateTime scheduledStartAt;
    @JsonProperty("scheduled_end_at")
    private LocalDateTime scheduledEndAt;
    
    // Duration information
    @JsonProperty("estimated_duration_minutes")
    private Integer estimatedDurationMinutes;
    @JsonProperty("buffer_minutes")
    private Integer bufferMinutes;
    
    // Pricing information
    @JsonProperty("total_price")
    private BigDecimal totalPrice;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("deposit_amount")
    private BigDecimal depositAmount;
    
    // Status information
    @JsonProperty("payment_status")
    private Booking.PaymentStatus paymentStatus;
    @JsonProperty("status")
    private Booking.BookingStatus status;
    @JsonProperty("priority")
    private Booking.Priority priority;
    
    // Additional information
    @JsonProperty("coupon_code")
    private String couponCode;
    @JsonProperty("notes")
    private String notes;
    @JsonProperty("special_requests")
    private List<String> specialRequests;
}
