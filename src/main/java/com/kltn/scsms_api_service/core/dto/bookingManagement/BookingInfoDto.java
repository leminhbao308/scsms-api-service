package com.kltn.scsms_api_service.core.dto.bookingManagement;

import com.kltn.scsms_api_service.core.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    
    private UUID bookingId;
    private String bookingCode;
    
    // Customer information
    private UUID customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    
    // Vehicle information
    private UUID vehicleId;
    private String vehicleLicensePlate;
    private String vehicleBrandName;
    private String vehicleModelName;
    private String vehicleTypeName;
    private Integer vehicleYear;
    private String vehicleColor;
    
    // Branch and slot information
    private UUID branchId;
    private String branchName;
    private String branchCode;
    private UUID bayId;
    private String bayName;
    
    // Scheduling information
    private LocalDateTime preferredStartAt;
    private LocalDateTime scheduledStartAt;
    private LocalDateTime scheduledEndAt;
    private LocalDateTime actualCheckInAt;
    private LocalDateTime actualStartAt;
    private LocalDateTime actualEndAt;
    
    // Duration information
    private Integer estimatedDurationMinutes;
    private Integer bufferMinutes;
    private Long actualDurationMinutes;
    
    // Pricing information
    private BigDecimal totalPrice;
    private String currency;
    private BigDecimal depositAmount;
    
    // Status information
    private Booking.PaymentStatus paymentStatus;
    private Booking.BookingStatus status;
    private Booking.Priority priority;
    
    // Additional information
    private String couponCode;
    private String notes;
    private List<String> specialRequests;
    
    // Cancellation information
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private String cancelledBy;
    
    // Audit information
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String modifiedBy;
    
    // Related data
    private List<BookingItemInfoDto> bookingItems;
    private List<BookingAssignmentInfoDto> assignments;
    private List<BookingPaymentInfoDto> payments;
    
    // Computed fields
    private Boolean isActive;
    private Boolean isCancelled;
    private Boolean isCompleted;
    private Boolean needsPayment;
    private Boolean isFullyPaid;
    private Integer totalEstimatedDuration;
}
