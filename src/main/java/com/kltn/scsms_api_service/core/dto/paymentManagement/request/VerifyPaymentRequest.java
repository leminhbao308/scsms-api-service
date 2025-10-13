package com.kltn.scsms_api_service.core.dto.paymentManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPaymentRequest {
    
    @JsonProperty("order_code")
    private Long orderCode;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("cancel")
    private Boolean cancel;
}
