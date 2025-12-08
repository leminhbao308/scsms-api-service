package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.saleOrderManagement.SaleReturnInfoDto;
import com.kltn.scsms_api_service.core.entity.SalesReturn;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        AuditMapper.class,
        SalesReturnLineMapper.class,
        SaleOrderMapper.class,
        BranchMapper.class}
)
public interface SalesReturnMapper {
    SalesReturn toEntity(SaleReturnInfoDto saleReturnInfoDto);
    
    @Mapping(target = "branch", qualifiedByName = "toBranchInfoDto")
    SaleReturnInfoDto toSaleReturnInfoDto(SalesReturn salesReturn);
}
