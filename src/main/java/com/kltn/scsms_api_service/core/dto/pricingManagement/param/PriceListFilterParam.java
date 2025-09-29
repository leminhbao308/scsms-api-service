package com.kltn.scsms_api_service.core.dto.pricingManagement.param;

import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import com.kltn.scsms_api_service.core.entity.enumAttribute.ApprovalStatus;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PriceListStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceListFilterParam extends BaseFilterParam {
    
    private String priceListCode;
    
    private String priceListName;
    
    private UUID branchId;
    
    private PriceListStatus status;
    
    private ApprovalStatus approvalStatus;
    
    private Boolean isDefault;
    
    private String currency;
    
    private LocalDateTime effectiveFromStart;
    
    private LocalDateTime effectiveFromEnd;
    
    private LocalDateTime effectiveToStart;
    
    private LocalDateTime effectiveToEnd;
    
    private UUID createdBy;
    
    private LocalDateTime createdFromDate;
    
    private LocalDateTime createdToDate;
}
