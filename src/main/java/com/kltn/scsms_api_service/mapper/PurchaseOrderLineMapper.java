package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.PurchaseOrderLineInfoDto;
import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.request.CreatePOLine;
import com.kltn.scsms_api_service.core.entity.Product;
import com.kltn.scsms_api_service.core.entity.PurchaseOrderLine;
import com.kltn.scsms_api_service.core.entity.Supplier;
import com.kltn.scsms_api_service.core.service.entityService.ProductService;
import com.kltn.scsms_api_service.core.service.entityService.SupplierService;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class, ProductMapper.class, SupplierMapper.class}
)
public interface PurchaseOrderLineMapper {
    
    PurchaseOrderLineInfoDto toPurchaseOrderLineInfoDto(PurchaseOrderLine purchaseOrderLine);
    
    default PurchaseOrderLine toEntity(CreatePOLine createPOLine, ProductService productES, SupplierService supplierES) {
        Product productRef = productES.getRefByProductId(createPOLine.getProductId());
        Supplier supplierRef = supplierES.getRefById(createPOLine.getSupplierId());
        return PurchaseOrderLine.builder()
            .product(productRef)
            .supplier(supplierRef)
            .quantityOrdered(createPOLine.getQty())
            .unitCost(createPOLine.getUnitCost())
            .lotCode(createPOLine.getLotCode())
            .expiryDate(createPOLine.getExpiryDate())
            .build();
    }
}
