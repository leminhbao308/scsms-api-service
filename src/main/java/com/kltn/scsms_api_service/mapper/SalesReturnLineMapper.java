package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.saleOrderManagement.SaleReturnLineInfoDto;
import com.kltn.scsms_api_service.core.entity.SalesReturnLine;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        AuditMapper.class,
        ProductMapper.class}
)
public interface SalesReturnLineMapper {
    SalesReturnLine toEntity(SaleReturnLineInfoDto saleReturnLineInfoDto);
    
    SaleReturnLineInfoDto toSaleReturnLineInfoDto(SalesReturnLine salesReturnLine);
    
}
