package com.kltn.scsms_api_service.core.dto.servicePackageManagement.request;

import jakarta.validation.constraints.NotNull;
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
public class CreateServicePackageServiceRequest {
    
    @NotNull(message = "Service ID is required")
    private UUID serviceId;
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
    
    private BigDecimal unitPrice;
    
    private String notes;
    
    @Builder.Default
    private Boolean isRequired = true;
}
