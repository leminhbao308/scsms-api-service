package com.kltn.scsms_api_service.core.dto.serviceManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO để cập nhật ServiceProduct
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceProductRequest {
    
    @JsonProperty("product_id")
    private java.util.UUID productId;
    
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    @JsonProperty("quantity")
    private BigDecimal quantity;
    
    @JsonProperty("unit")
    private String unit;
    
    @JsonProperty("notes")
    private String notes;
    
    @JsonProperty("is_required")
    private Boolean isRequired;
    
    @JsonProperty("sort_order")
    private Integer sortOrder;
}
