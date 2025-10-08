package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.warehouseManagement.response.WarehouseInfoDto;
import com.kltn.scsms_api_service.core.entity.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        BranchMapper.class,
        AuditMapper.class
    })
public interface WarehouseMapper {
    
        WarehouseInfoDto toWarehouseInfoDto(Warehouse warehouse);
}
