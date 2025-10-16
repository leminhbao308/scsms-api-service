package com.kltn.scsms_api_service.core.dto.saleOrderManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kltn.scsms_api_service.core.dto.branchManagement.BranchInfoDto;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import com.kltn.scsms_api_service.core.dto.userManagement.UserInfoDto;
import com.kltn.scsms_api_service.core.entity.enumAttribute.SalesStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SaleOrderInfoDto extends AuditDto {
    private UUID id;
    
    private UserInfoDto customer;
    
    private BranchInfoDto branch;
    
    private SalesStatus status;
    
    private List<SaleOrderLineInfoDto> lines;
}
