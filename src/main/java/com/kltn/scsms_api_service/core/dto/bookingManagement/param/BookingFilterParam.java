package com.kltn.scsms_api_service.core.dto.bookingManagement.param;

import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import com.kltn.scsms_api_service.core.entity.Booking;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class BookingFilterParam extends BaseFilterParam<BookingFilterParam> {
    
    // Customer filters
    private UUID customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    
    // Vehicle filters
    private UUID vehicleId;
    private String vehicleLicensePlate;
    private String vehicleBrandName;
    private String vehicleModelName;
    private String vehicleTypeName;
    
    // Branch and slot filters
    private UUID branchId;
    private UUID slotId;
    private String slotCategory;
    
    // Status filters
    private Booking.BookingStatus status;
    private Booking.PaymentStatus paymentStatus;
    private Booking.Priority priority;
    
    // Date filters
    private LocalDate bookingDate;
    private LocalDate bookingDateFrom;
    private LocalDate bookingDateTo;
    private LocalDateTime scheduledStartFrom;
    private LocalDateTime scheduledStartTo;
    private LocalDateTime scheduledEndFrom;
    private LocalDateTime scheduledEndTo;
    
    // Price filters
    private java.math.BigDecimal totalPriceFrom;
    private java.math.BigDecimal totalPriceTo;
    
    // Additional filters
    private String couponCode;
    private String notes;
    private Boolean hasSpecialRequests;
    
    // Staff filters
    private UUID staffId;
    private String staffRole;
    
    // Payment filters
    private String paymentMethod;
    private String transactionId;
    
    // Audit filters
    private Boolean isActive;
    private Boolean isDeleted;
    
}
