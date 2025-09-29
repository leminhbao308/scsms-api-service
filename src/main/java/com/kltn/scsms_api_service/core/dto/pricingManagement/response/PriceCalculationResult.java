package com.kltn.scsms_api_service.core.dto.pricingManagement.response;

import com.kltn.scsms_api_service.core.entity.enumAttribute.ItemType;
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
public class PriceCalculationResult {
    private UUID itemId;
    private ItemType itemType;
    private String itemName;
    private BigDecimal quantity;
    private BigDecimal basePrice;
    private BigDecimal effectivePrice;
    private BigDecimal discountAmount;
    private BigDecimal discountPercentage;
    private BigDecimal lineTotal;
    private String priceSource; // "BASE", "TIER", "CUSTOMER_OVERRIDE", "COMBO"
    private List<String> appliedConditions;
}
