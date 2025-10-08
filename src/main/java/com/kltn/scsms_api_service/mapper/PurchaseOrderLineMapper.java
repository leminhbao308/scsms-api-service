package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.PurchaseOrderLineInfoDto;
import com.kltn.scsms_api_service.core.entity.PurchaseOrderLine;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class, ProductMapper.class, SupplierMapper.class}
)
public interface PurchaseOrderLineMapper {
    
    PurchaseOrderLineInfoDto toPurchaseOrderLineInfoDto(PurchaseOrderLine purchaseOrderLine);
}
