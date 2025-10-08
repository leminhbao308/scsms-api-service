package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.inventoryManagement.InventoryLevelInfoDto;
import com.kltn.scsms_api_service.core.entity.InventoryLevel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class, ProductMapper.class, WarehouseMapper.class}
)
public interface InventoryLevelMapper {
    
    InventoryLevel toEntity(InventoryLevelInfoDto inventoryLevelInfoDto);
    
    InventoryLevelInfoDto toInventoryLevelInfoDto(InventoryLevel inventoryLevel);
}
