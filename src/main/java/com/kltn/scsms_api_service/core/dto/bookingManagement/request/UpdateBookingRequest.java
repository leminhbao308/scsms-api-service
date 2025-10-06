package com.kltn.scsms_api_service.core.dto.bookingManagement.request;

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
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    
    // Vehicle information
    private String vehicleLicensePlate;
    private String vehicleBrandName;
    private String vehicleModelName;
    private String vehicleTypeName;
    private Integer vehicleYear;
    private String vehicleColor;
    
    // Scheduling information
    private LocalDateTime preferredStartAt;
    private LocalDateTime scheduledStartAt;
    private LocalDateTime scheduledEndAt;
    
    // Duration information
    private Integer estimatedDurationMinutes;
    private Integer bufferMinutes;
    
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
}
