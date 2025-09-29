package com.kltn.scsms_api_service.core.dto.pricingManagement.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreatePriceListRequest {
    
    @NotBlank(message = "Price list code is required")
    private String priceListCode;
    
    @NotBlank(message = "Price list name is required")
    private String priceListName;
    
    @NotNull(message = "Branch ID is required")
    private UUID branchId;
    
    @NotNull(message = "Effective from date is required")
    private LocalDateTime effectiveFrom;
    
    private LocalDateTime effectiveTo;
    
    @Builder.Default
    private String currency = "VND";
    
    @Builder.Default
    private Boolean isDefault = false;
    
    @Builder.Default
    private Integer priority = 1;
    
    private String description;
    
    private List<CreatePriceListLineRequest> priceListLines;
    
    private List<CreatePriceListComboRequest> combos;
}
