package com.kltn.scsms_api_service.core.dto.bookingManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.Booking;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO để tạo booking mới
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    
    // Customer information
    @JsonProperty("customer_id")
    private UUID customerId; // nullable nếu là guest
    @NotNull(message = "Customer name is required")
    @JsonProperty("customer_name")
    private String customerName;
    @NotNull(message = "Customer phone is required")
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
    
    // Branch and slot information
    @NotNull(message = "Branch ID is required")
    @JsonProperty("branch_id")
    private UUID branchId;
    @JsonProperty("bay_id")
    private UUID bayId; // nullable, sẽ được assign sau
    
    // Scheduling information
    @NotNull(message = "Preferred start time is required")
    @JsonProperty("preferred_start_at")
    private LocalDateTime preferredStartAt;
    @JsonProperty("scheduled_start_at")
    private LocalDateTime scheduledStartAt; // nullable, sẽ được set khi confirm
    @JsonProperty("scheduled_end_at")
    private LocalDateTime scheduledEndAt; // nullable, sẽ được tính toán
    
    // Duration information
    @JsonProperty("estimated_duration_minutes")
    private Integer estimatedDurationMinutes;
    @Builder.Default
    @JsonProperty("buffer_minutes")
    private Integer bufferMinutes = 15;
    
    // Pricing information
    @JsonProperty("total_price")
    private BigDecimal totalPrice;
    @Builder.Default
    @JsonProperty("currency")
    private String currency = "VND";
    @JsonProperty("deposit_amount")
    private BigDecimal depositAmount;
    
    // Status information
    @Builder.Default
    @JsonProperty("payment_status")
    private Booking.PaymentStatus paymentStatus = Booking.PaymentStatus.PENDING;
    @Builder.Default
    @JsonProperty("status")
    private Booking.BookingStatus status = Booking.BookingStatus.PENDING;
    @Builder.Default
    @JsonProperty("priority")
    private Booking.Priority priority = Booking.Priority.NORMAL;
    
    // Additional information
    @JsonProperty("coupon_code")
    private String couponCode;
    @JsonProperty("notes")
    private String notes;
    @JsonProperty("special_requests")
    private List<String> specialRequests;
    
    // Booking items
    @Valid
    @NotNull(message = "Booking items are required")
    @JsonProperty("booking_items")
    private List<CreateBookingItemRequest> bookingItems;
    
    // Assignments (optional, có thể được tạo sau)
    @JsonProperty("assignments")
    private List<CreateBookingAssignmentRequest> assignments;
    
    // Payments (optional, có thể được tạo sau)
    @JsonProperty("payments")
    private List<CreateBookingPaymentRequest> payments;
}
