package com.kltn.scsms_api_service.core.dto.servicePackageManagement.request;

import jakarta.validation.constraints.Positive;
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
public class UpdateServicePackageServiceRequest {
    
    private UUID servicePackageServiceId;
    
    private UUID serviceId;
    
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
    
    private BigDecimal unitPrice;
    
    private String notes;
    
    private Boolean isRequired;
}
