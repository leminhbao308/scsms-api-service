package com.kltn.scsms_api_service.core.dto.pricingManagement;

import com.kltn.scsms_api_service.core.entity.enumAttribute.ItemType;
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
public class PriceImportRow {
    private ItemType itemType;
    private UUID itemId;
    private String itemCode;
    private String itemName;
    private BigDecimal basePrice;
    private BigDecimal costPrice;
    private BigDecimal minPrice;
    private BigDecimal maxDiscountPercentage;
    private Boolean taxInclusive;
}
