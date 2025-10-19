package com.kltn.scsms_api_service.core.dto.bookingManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.CreateBookingItemRequest;
import com.kltn.scsms_api_service.core.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO chứa thông tin chi tiết của booking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingInfoDto {

    @JsonProperty("booking_id")
    private UUID bookingId;

    @JsonProperty("booking_code")
    private String bookingCode;

    // Customer information
    @JsonProperty("customer_id")
    private UUID customerId;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("customer_phone")
    private String customerPhone;

    @JsonProperty("customer_email")
    private String customerEmail;

    // Vehicle information
    @JsonProperty("vehicle_id")
    private UUID vehicleId;

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
    @JsonProperty("branch_id")
    private UUID branchId;

    @JsonProperty("branch_name")
    private String branchName;

    @JsonProperty("branch_code")
    private String branchCode;

    @JsonProperty("bay_id")
    private UUID bayId;

    @JsonProperty("bay_name")
    private String bayName;

    // Slot information
    @JsonProperty("slot_id")
    private UUID slotId;

    @JsonProperty("slot_start_time")
    private LocalTime slotStartTime;

    @JsonProperty("slot_end_time")
    private LocalTime slotEndTime;

    @JsonProperty("slot_duration_minutes")
    private Integer slotDurationMinutes;

    @JsonProperty("slot_status")
    private String slotStatus;

    // Scheduling information
    @JsonProperty("preferred_start_at")
    private LocalDateTime preferredStartAt;

    @JsonProperty("scheduled_start_at")
    private LocalDateTime scheduledStartAt;

    @JsonProperty("scheduled_end_at")
    private LocalDateTime scheduledEndAt;

    @JsonProperty("actual_check_in_at")
    private LocalDateTime actualCheckInAt;

    @JsonProperty("actual_start_at")
    private LocalDateTime actualStartAt;

    @JsonProperty("actual_end_at")
    private LocalDateTime actualEndAt;

    // Duration information
    @JsonProperty("estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @JsonProperty("buffer_minutes")
    private Integer bufferMinutes;

    @JsonProperty("actual_duration_minutes")
    private Long actualDurationMinutes;

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

    // Cancellation information
    @JsonProperty("cancellation_reason")
    private String cancellationReason;

    @JsonProperty("cancelled_at")
    private LocalDateTime cancelledAt;

    @JsonProperty("cancelled_by")
    private String cancelledBy;

    // Audit information
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("modified_by")
    private String modifiedBy;

    // Related data
    @JsonProperty("booking_items")
    private List<CreateBookingItemRequest> bookingItems;

    // Computed fields
    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("is_cancelled")
    private Boolean isCancelled;

    @JsonProperty("is_completed")
    private Boolean isCompleted;

    @JsonProperty("needs_payment")
    private Boolean needsPayment;

    @JsonProperty("is_fully_paid")
    private Boolean isFullyPaid;

    @JsonProperty("total_estimated_duration")
    private Integer totalEstimatedDuration;
}
