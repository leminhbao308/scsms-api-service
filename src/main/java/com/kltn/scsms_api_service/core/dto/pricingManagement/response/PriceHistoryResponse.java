package com.kltn.scsms_api_service.core.dto.pricingManagement.response;

import com.kltn.scsms_api_service.core.entity.enumAttribute.ChangeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistoryResponse {
    
    private UUID historyId;
    
    private UUID priceListId;
    
    private String priceListName;
    
    private UUID lineId;
    
    private String itemName;
    
    private ChangeType changeType;
    
    private BigDecimal oldPrice;
    
    private BigDecimal newPrice;
    
    private String oldValue;
    
    private String newValue;
    
    private UUID changedBy;
    
    private String changedByName;
    
    private LocalDateTime changedAt;
    
    private String changeReason;
}
