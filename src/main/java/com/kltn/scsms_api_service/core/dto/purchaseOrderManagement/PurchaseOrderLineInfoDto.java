package com.kltn.scsms_api_service.core.dto.purchaseOrderManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.productManagement.ProductInfoDto;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import com.kltn.scsms_api_service.core.dto.supplierManagement.SupplierInfoDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PurchaseOrderLineInfoDto extends AuditDto {
    
    private UUID id;
    
    private ProductInfoDto product;
    
    private SupplierInfoDto supplier;
    
    @JsonProperty("qty_ordered")
    private Long quantityOrdered;
    
    @JsonProperty("unit_cost")
    private BigDecimal unitCost;
    
    @JsonProperty("lot_code")
    private String lotCode;
    
    @JsonProperty("expiry_date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime expiryDate;
}
