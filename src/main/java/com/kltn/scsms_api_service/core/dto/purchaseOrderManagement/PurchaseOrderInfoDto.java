package com.kltn.scsms_api_service.core.dto.purchaseOrderManagement;

import com.kltn.scsms_api_service.core.dto.branchManagement.BranchInfoDto;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PurchaseOrderInfoDto extends AuditDto {
    
    private UUID id;
    
    private BranchInfoDto branch;
    
    private List<PurchaseOrderLineInfoDto> lines;
}
