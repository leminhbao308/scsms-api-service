package com.kltn.scsms_api_service.core.dto.warehouseManagement.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kltn.scsms_api_service.core.dto.branchManagement.BranchFlatDto;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WarehouseInfoDto extends AuditDto {
    
    private String id;
    
    private BranchFlatDto branch;
    
    private boolean locked;
}
