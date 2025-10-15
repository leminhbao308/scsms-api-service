package com.kltn.scsms_api_service.core.dto.paymentManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InitiatePaymentRequest {
    @JsonProperty("sales_order_id")
    private UUID salesOrderId;
    
    @JsonProperty("booking_id")
    private UUID bookingId;
    
    @JsonProperty("amount")
    private Integer amount;
    
    @JsonProperty("payment_method")
    private PaymentMethod paymentMethod;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("return_url")
    private String returnUrl;
    
    @JsonProperty("cancel_url")
    private String cancelUrl;
    
    @JsonProperty("callback_url")
    private String callbackUrl;
}
