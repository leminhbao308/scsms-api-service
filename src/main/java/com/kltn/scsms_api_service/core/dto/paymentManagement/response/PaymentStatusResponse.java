package com.kltn.scsms_api_service.core.dto.paymentManagement.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResponse {
    
    @JsonProperty("payment_id")
    private String paymentId;
    
    @JsonProperty("sales_order_id")
    private String salesOrderId;
    
    @JsonProperty("status")
    private PaymentStatus status;
    
    @JsonProperty("amount")
    private Integer amount;
    
    @JsonProperty("transaction_id")
    private String transactionId;
    
    @JsonProperty("paid_at")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime paidAt;
    
    @JsonProperty("message")
    private String message;
}
