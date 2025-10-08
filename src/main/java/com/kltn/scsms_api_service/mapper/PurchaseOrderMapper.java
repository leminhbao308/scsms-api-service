package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.PurchaseOrderInfoDto;
import com.kltn.scsms_api_service.core.entity.PurchaseOrder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class, PurchaseOrderLineMapper.class, BranchMapper.class, WarehouseMapper.class}
)
public interface PurchaseOrderMapper {
    PurchaseOrderInfoDto toPurchaseOrderInfoDto(PurchaseOrder purchaseOrder);
}
