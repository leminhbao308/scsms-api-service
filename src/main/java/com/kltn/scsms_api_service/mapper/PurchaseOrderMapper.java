package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.PurchaseOrderInfoDto;
import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.request.CreatePORequest;
import com.kltn.scsms_api_service.core.entity.Branch;
import com.kltn.scsms_api_service.core.entity.PurchaseOrder;
import com.kltn.scsms_api_service.core.service.entityService.BranchService;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class, PurchaseOrderLineMapper.class, BranchMapper.class}
)
public interface PurchaseOrderMapper {
    
    PurchaseOrderInfoDto toPurchaseOrderInfoDto(PurchaseOrder purchaseOrder);
    
    default PurchaseOrder toEntity(CreatePORequest request, BranchService branchES) {
        Branch branchRef = branchES.getRefById(request.getBranchId());
        return PurchaseOrder.builder()
            .branch(branchRef)
            .build();
    }
}
