package com.kltn.scsms_api_service.core.dto.serviceManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO để tạo ServiceProduct
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceProductRequest {
    
    @NotNull(message = "Product ID is required")
    @JsonProperty("product_id")
    private java.util.UUID productId;
    
    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    @JsonProperty("quantity")
    private BigDecimal quantity;
    
    @JsonProperty("unit")
    private String unit;
    
    @JsonProperty("notes")
    private String notes;
    
    @JsonProperty("is_required")
    private Boolean isRequired = true;
    
    @JsonProperty("sort_order")
    private Integer sortOrder = 0;
}
