package com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePOLine {
    @JsonProperty("product_id")
    private UUID productId;
    
    @JsonProperty("supplier_id")
    private UUID supplierId;
    
    private Long qty;
    
    @JsonProperty("unit_cost")
    private BigDecimal unitCost;
    
    @JsonProperty("lot_code")
    private String lotCode;
    
    @JsonProperty("expiry_date")
    private LocalDateTime expiryDate;
}
