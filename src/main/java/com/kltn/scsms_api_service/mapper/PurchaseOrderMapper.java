package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.PurchaseOrderInfoDto;
import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.request.CreatePORequest;
import com.kltn.scsms_api_service.core.entity.Branch;
import com.kltn.scsms_api_service.core.entity.PurchaseOrder;
import com.kltn.scsms_api_service.core.entity.Warehouse;
import com.kltn.scsms_api_service.core.service.entityService.BranchService;
import com.kltn.scsms_api_service.core.service.entityService.WarehouseEntityService;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class, PurchaseOrderLineMapper.class, BranchMapper.class, WarehouseMapper.class}
)
public interface PurchaseOrderMapper {
    
    PurchaseOrderInfoDto toPurchaseOrderInfoDto(PurchaseOrder purchaseOrder);
    
    default PurchaseOrder toEntity(CreatePORequest request, BranchService branchES, WarehouseEntityService warehouseES) {
        Branch branchRef = branchES.getRefById(request.getBranchId());
        Warehouse warehouseRef = warehouseES.getRefByWarehouseId(request.getWarehouseId());
        return PurchaseOrder.builder()
            .branch(branchRef)
            .warehouse(warehouseRef)
            .build();
    }
}
