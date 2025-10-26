package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.saleOrderManagement.SaleOrderLineInfoDto;
import com.kltn.scsms_api_service.core.entity.SalesOrderLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { AuditMapper.class,
        ProductMapper.class })
public interface SaleOrderLineMapper {

    @Mapping(target = "isServiceItem", expression = "java(saleOrderLine.isServiceItem())")
    SaleOrderLineInfoDto toSaleOrderLineInfoDto(SalesOrderLine saleOrderLine);
}
