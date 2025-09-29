package com.kltn.scsms_api_service.core.dto.serviceManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateServiceProductRequest {
    
    @JsonProperty("service_product_id")
    private UUID serviceProductId; // ID của ServiceProduct (null nếu là thêm mới)
    
    @JsonProperty("product_id")
    private UUID productId; // ID của Product (bắt buộc nếu thêm mới)
    
    @Min(value = 1, message = "Quantity must be at least 1")
    @JsonProperty("quantity")
    private Integer quantity;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    @JsonProperty("unit_price")
    private BigDecimal unitPrice;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @JsonProperty("notes")
    private String notes;
    
    @JsonProperty("is_required")
    private Boolean isRequired;
    
    
    @JsonProperty("is_active")
    private Boolean isActive;
}
