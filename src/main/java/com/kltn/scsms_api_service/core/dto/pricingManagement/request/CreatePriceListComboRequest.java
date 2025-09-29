package com.kltn.scsms_api_service.core.dto.pricingManagement.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePriceListComboRequest {
    
    @NotBlank(message = "Combo code is required")
    private String comboCode;
    
    @NotBlank(message = "Combo name is required")
    private String comboName;
    
    @NotNull(message = "Combo price is required")
    @DecimalMin(value = "0.0", message = "Combo price must be non-negative")
    private BigDecimal comboPrice;
    
    @DecimalMin(value = "0.0", message = "Discount percentage must be non-negative")
    private BigDecimal discountPercentage;
    
    private LocalDateTime effectiveFrom;
    
    private LocalDateTime effectiveTo;
    
    @Builder.Default
    private Boolean isActive = true;
    
    private String description;
    
    private List<UUID> comboItems;
}
