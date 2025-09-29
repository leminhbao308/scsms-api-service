package com.kltn.scsms_api_service.core.dto.pricingManagement.response;

import com.kltn.scsms_api_service.core.entity.enumAttribute.ApprovalStatus;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PriceListStatus;
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
public class PriceListResponse {
    
    private UUID priceListId;
    
    private String priceListCode;
    
    private String priceListName;
    
    private UUID branchId;
    
    private String branchName;
    
    private LocalDateTime effectiveFrom;
    
    private LocalDateTime effectiveTo;
    
    private String currency;
    
    private Boolean isDefault;
    
    private Integer priority;
    
    private PriceListStatus status;
    
    private ApprovalStatus approvalStatus;
    
    private UUID approvedBy;
    
    private String approvedByName;
    
    private LocalDateTime approvedAt;
    
    private String description;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private UUID createdBy;
    
    private UUID updatedBy;
    
    private String createdByName;
    
    private String updatedByName;
    
    private Boolean isActive;
    
    private List<PriceListLineResponse> priceListLines;
    
    private List<PriceListComboResponse> combos;
    
    private Integer totalLines;
    
    private Integer totalCombos;
}
