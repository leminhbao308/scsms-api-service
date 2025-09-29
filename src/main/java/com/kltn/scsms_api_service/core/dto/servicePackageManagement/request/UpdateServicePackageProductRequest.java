package com.kltn.scsms_api_service.core.dto.servicePackageManagement.request;

import jakarta.validation.constraints.Min;
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
public class UpdateServicePackageProductRequest {
    
    private UUID servicePackageProductId;
    private UUID productId;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    @Min(value = 0, message = "Unit price must be non-negative")
    private BigDecimal unitPrice;
    
    private String notes;
    private Boolean isRequired;
    private Boolean isActive;
}
