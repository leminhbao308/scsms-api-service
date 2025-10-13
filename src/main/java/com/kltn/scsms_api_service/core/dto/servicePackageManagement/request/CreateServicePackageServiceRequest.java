package com.kltn.scsms_api_service.core.dto.servicePackageManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("service_id")
    private UUID serviceId;
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @JsonProperty("quantity")
    private Integer quantity;
    
    @JsonProperty("unit_price")
    private BigDecimal unitPrice;
    
    private String notes;
    
    @JsonProperty("is_required")
    @Builder.Default
    private Boolean isRequired = true;
}
