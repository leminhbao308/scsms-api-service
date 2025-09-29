package com.kltn.scsms_api_service.core.dto.pricingManagement.response;

import com.kltn.scsms_api_service.core.dto.pricingManagement.request.PriceCalculationItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComboSuggestion {
    private UUID comboId;
    private String comboCode;
    private String comboName;
    private BigDecimal comboPrice;
    private BigDecimal regularTotal;
    private BigDecimal originalPrice; // Add missing property
    private BigDecimal savings;
    private BigDecimal savingsAmount; // Add missing property (alias for savings)
    private BigDecimal savingsPercentage;
    private List<PriceCalculationItem> requiredItems;
    private List<PriceCalculationItem> optionalItems;
    private boolean isApplicable;
    private String reason;
    private String description; // Add missing property
    
    // Convenience methods for backwards compatibility
    public BigDecimal getSavingsAmount() {
        return savings != null ? savings : BigDecimal.ZERO;
    }
    
    public void setSavingsAmount(BigDecimal savingsAmount) {
        this.savings = savingsAmount;
        this.savingsAmount = savingsAmount;
    }
}
