package com.kltn.scsms_api_service.core.dto.bookingManagement.request;

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
    private UUID customerId; // nullable nếu là guest
    @NotNull(message = "Customer name is required")
    private String customerName;
    @NotNull(message = "Customer phone is required")
    private String customerPhone;
    private String customerEmail;
    
    // Vehicle information
    private UUID vehicleId; // nullable nếu chưa có profile
    private String vehicleLicensePlate;
    private String vehicleBrandName;
    private String vehicleModelName;
    private String vehicleTypeName;
    private Integer vehicleYear;
    private String vehicleColor;
    
    // Branch and slot information
    @NotNull(message = "Branch ID is required")
    private UUID branchId;
    private UUID slotId; // nullable, sẽ được assign sau
    
    // Scheduling information
    @NotNull(message = "Preferred start time is required")
    private LocalDateTime preferredStartAt;
    private LocalDateTime scheduledStartAt; // nullable, sẽ được set khi confirm
    private LocalDateTime scheduledEndAt; // nullable, sẽ được tính toán
    
    // Duration information
    private Integer estimatedDurationMinutes;
    @Builder.Default
    private Integer bufferMinutes = 15;
    
    // Pricing information
    private BigDecimal totalPrice;
    @Builder.Default
    private String currency = "VND";
    private BigDecimal depositAmount;
    
    // Status information
    @Builder.Default
    private Booking.PaymentStatus paymentStatus = Booking.PaymentStatus.PENDING;
    @Builder.Default
    private Booking.BookingStatus status = Booking.BookingStatus.PENDING;
    @Builder.Default
    private Booking.Priority priority = Booking.Priority.NORMAL;
    
    // Additional information
    private String couponCode;
    private String notes;
    private List<String> specialRequests;
    
    // Booking items
    @Valid
    @NotNull(message = "Booking items are required")
    private List<CreateBookingItemRequest> bookingItems;
    
    // Assignments (optional, có thể được tạo sau)
    private List<CreateBookingAssignmentRequest> assignments;
    
    // Payments (optional, có thể được tạo sau)
    private List<CreateBookingPaymentRequest> payments;
}
