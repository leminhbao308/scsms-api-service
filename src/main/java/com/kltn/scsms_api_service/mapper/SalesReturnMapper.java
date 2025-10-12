package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.saleOrderManagement.SaleReturnInfoDto;
import com.kltn.scsms_api_service.core.entity.SalesReturn;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        AuditMapper.class,
        SalesReturnLineMapper.class,
        SaleOrderMapper.class,
        WarehouseMapper.class}
)
public interface SalesReturnMapper {
    SalesReturn toEntity(SaleReturnInfoDto saleReturnInfoDto);
    
    SaleReturnInfoDto toSaleReturnInfoDto(SalesReturn salesReturn);
}
