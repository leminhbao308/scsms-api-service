package com.kltn.scsms_api_service.core.dto.pricingManagement.request;

import com.kltn.scsms_api_service.core.entity.enumAttribute.ItemType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PriceCalculationItem {
    private ItemType itemType;
    private UUID itemId;
    private BigDecimal quantity;
    private String itemName;
}
