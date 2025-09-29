package com.kltn.scsms_api_service.core.dto.priceManagement.response;

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
public class PriceListDetailResponse {
    private UUID priceListDetailId;
    private UUID priceListId;
    private ItemType itemType;
    private UUID itemId;
    private String itemName;
    private BigDecimal basePrice;
    private BigDecimal sellingPrice;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String unitOfMeasure;
    private Integer minQuantity;
    private Integer maxQuantity;
    private String tierPricing;
    private String conditions;
    private String notes;
}
