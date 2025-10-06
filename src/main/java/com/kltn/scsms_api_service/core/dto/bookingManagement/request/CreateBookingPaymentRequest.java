package com.kltn.scsms_api_service.core.dto.bookingManagement.request;

import com.kltn.scsms_api_service.core.entity.BookingPayment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO để tạo booking payment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingPaymentRequest {
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private BookingPayment.PaymentMethod paymentMethod;
    
    @Builder.Default
    private BookingPayment.PaymentStatus paymentStatus = BookingPayment.PaymentStatus.PENDING;
    
    private String transactionId;
    private String referenceCode;
    private LocalDateTime paidAt;
    private LocalDateTime expiresAt;
    
    private String cardInfo;
    private String notes;
    
    @Builder.Default
    private BigDecimal transactionFee = BigDecimal.ZERO;
    
    private BigDecimal exchangeRate;
    private String originalCurrency;
    private BigDecimal originalAmount;
    
    // Payer information
    private String payerName;
    private String payerPhone;
    private String payerEmail;
}
