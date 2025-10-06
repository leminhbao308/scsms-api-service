package com.kltn.scsms_api_service.core.dto.bookingManagement;

import com.kltn.scsms_api_service.core.entity.BookingPayment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO chứa thông tin chi tiết của booking payment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingPaymentInfoDto {
    
    private UUID paymentId;
    private UUID bookingId;
    
    // Payment information
    private BigDecimal amount;
    private BookingPayment.PaymentMethod paymentMethod;
    private BookingPayment.PaymentStatus paymentStatus;
    
    // Transaction information
    private String transactionId;
    private String referenceCode;
    private LocalDateTime paidAt;
    private LocalDateTime expiresAt;
    
    // Card information (masked)
    private String cardInfo;
    private String notes;
    
    // Fee information
    private BigDecimal transactionFee;
    private BigDecimal totalAmount;
    
    // Currency information
    private BigDecimal exchangeRate;
    private String originalCurrency;
    private BigDecimal originalAmount;
    
    // Refund information
    private BigDecimal refundAmount;
    private String refundReason;
    private LocalDateTime refundedAt;
    private String refundedBy;
    
    // Payer information
    private String payerName;
    private String payerPhone;
    private String payerEmail;
    
    // Audit information
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String modifiedBy;
    
    // Computed fields
    private Boolean isSuccessful;
    private Boolean isFailed;
    private Boolean isPending;
    private Boolean isRefunded;
}
