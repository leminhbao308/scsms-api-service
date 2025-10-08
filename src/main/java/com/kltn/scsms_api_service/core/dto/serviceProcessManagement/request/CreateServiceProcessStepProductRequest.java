package com.kltn.scsms_api_service.core.dto.serviceProcessManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateServiceProcessStepProductRequest {
    
    @NotNull(message = "Product ID is required")
    @JsonProperty("productId")
    private UUID productId;
    
    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    @JsonProperty("quantity")
    private BigDecimal quantity;
    
    @Size(max = 20, message = "Unit must not exceed 20 characters")
    @JsonProperty("unit")
    private String unit;
}
