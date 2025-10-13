package com.kltn.scsms_api_service.core.dto.servicePackageManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    @JsonProperty("service_package_service_id")
    private UUID servicePackageServiceId;
    
    @JsonProperty("service_id")
    private UUID serviceId;
    
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
    
    @JsonProperty("unit_price")
    private BigDecimal unitPrice;
    
    private String notes;
    
    @JsonProperty("is_required")
    private Boolean isRequired;
}
