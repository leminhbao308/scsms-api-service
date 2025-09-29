package com.kltn.scsms_api_service.core.dto.pricingManagement.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePriceListRequest {
    
    @NotBlank(message = "Price list name is required")
    private String priceListName;
    
    private UUID branchId;
    
    private LocalDateTime effectiveFrom;
    
    private LocalDateTime effectiveTo;
    
    private String currency;
    
    private Boolean isDefault;
    
    private Integer priority;
    
    private String description;
    
    private List<UpdatePriceListLineRequest> priceListLines;
    
    private List<UpdatePriceListComboRequest> combos;
}
