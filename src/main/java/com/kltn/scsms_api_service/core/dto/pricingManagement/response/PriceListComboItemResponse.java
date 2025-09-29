package com.kltn.scsms_api_service.core.dto.pricingManagement.response;

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
public class PriceListComboItemResponse {
    
    private UUID itemId;
    
    private ItemType itemType;
    
    private String itemName;
    
    private String itemCode;
    
    private BigDecimal itemPrice;
    
    private Integer quantity;
    
    private BigDecimal totalPrice;
}
