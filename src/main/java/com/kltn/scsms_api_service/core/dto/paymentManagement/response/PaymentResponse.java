package com.kltn.scsms_api_service.core.dto.paymentManagement.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import com.kltn.scsms_api_service.core.dto.saleOrderManagement.SaleOrderInfoDto;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PaymentMethod;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse extends AuditDto {
    
    @JsonProperty("payment_id")
    private UUID paymentId;
    
    @JsonProperty("sales_order")
    private SaleOrderInfoDto salesOrder;
    
    @JsonProperty("amount")
    private Integer amount;
    
    @JsonProperty("payment_url")
    private String paymentURL;
    
    @JsonProperty("order_code")
    private Long orderCode;
    
    @JsonProperty("status")
    private PaymentStatus status;
    
    @JsonProperty("payment_method")
    private PaymentMethod paymentMethod;
    
    @JsonProperty("qr_code")
    private String qrCode;
}
