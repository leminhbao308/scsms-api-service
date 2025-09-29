package com.kltn.scsms_api_service.core.dto.servicePackageManagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePackageProductDto {
    
    private UUID servicePackageProductId;
    private UUID packageId;
    private String packageName;
    private UUID productId;
    private String productName;
    private String productCode;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String notes;
    private Boolean isRequired;
    private Boolean isActive;
}
