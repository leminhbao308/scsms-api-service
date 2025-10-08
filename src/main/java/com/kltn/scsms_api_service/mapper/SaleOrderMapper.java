package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.saleOrderManagement.SaleOrderInfoDto;
import com.kltn.scsms_api_service.core.entity.SalesOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        AuditMapper.class,
        SaleOrderLineMapper.class,
        UserMapper.class,
        BranchMapper.class,
        WarehouseMapper.class}
)
public interface SaleOrderMapper {
    
    @Mapping(target = "branch", qualifiedByName = "toBranchInfoDto")
    SaleOrderInfoDto toSaleOrderInfoDto(SalesOrder saleOrder);
}
