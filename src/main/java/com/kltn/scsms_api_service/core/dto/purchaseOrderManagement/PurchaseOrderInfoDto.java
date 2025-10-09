package com.kltn.scsms_api_service.core.dto.purchaseOrderManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.branchManagement.BranchInfoDto;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import com.kltn.scsms_api_service.core.dto.warehouseManagement.response.WarehouseInfoDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
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
    
    private WarehouseInfoDto warehouse;
    
    private List<PurchaseOrderLineInfoDto> lines;
}
