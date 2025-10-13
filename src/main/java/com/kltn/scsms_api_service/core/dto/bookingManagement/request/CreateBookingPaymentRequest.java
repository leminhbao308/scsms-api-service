package com.kltn.scsms_api_service.core.dto.bookingManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.BookingPayment;
// import jakarta.validation.constraints.NotNull;
// import jakarta.validation.constraints.Positive;
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
    
    // @Positive(message = "Amount must be positive")
    @JsonProperty("amount")
    private BigDecimal amount;
    
    // @NotNull(message = "Payment method is required")    
    @JsonProperty("payment_method")
    private BookingPayment.PaymentMethod paymentMethod;
    
    @Builder.Default
    @JsonProperty("payment_status")
    private BookingPayment.PaymentStatus paymentStatus = BookingPayment.PaymentStatus.PENDING;
    
    @JsonProperty("transaction_id")
    private String transactionId;
    @JsonProperty("reference_code")
    private String referenceCode;
    @JsonProperty("paid_at")
    private LocalDateTime paidAt;
    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;
    
    @JsonProperty("card_info")
    private String cardInfo;
    @JsonProperty("notes")
    private String notes;
    
    @Builder.Default
    @JsonProperty("transaction_fee")
    private BigDecimal transactionFee = BigDecimal.ZERO;
    
    @JsonProperty("exchange_rate")
    private BigDecimal exchangeRate;
    @JsonProperty("original_currency")
    private String originalCurrency;
    @JsonProperty("original_amount")
    private BigDecimal originalAmount;
    
    // Payer information
    @JsonProperty("payer_name")
    private String payerName;
    @JsonProperty("payer_phone")
    private String payerPhone;
    @JsonProperty("payer_email")
    private String payerEmail;
}
