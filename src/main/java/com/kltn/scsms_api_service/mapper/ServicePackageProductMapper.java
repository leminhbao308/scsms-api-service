package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.servicePackageManagement.ServicePackageProductDto;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.CreateServicePackageProductRequest;
import com.kltn.scsms_api_service.core.dto.servicePackageManagement.request.UpdateServicePackageProductRequest;
import com.kltn.scsms_api_service.core.entity.ServicePackageProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class}
)
public interface ServicePackageProductMapper {
    
    @Mapping(target = "packageId", source = "servicePackage.packageId")
    @Mapping(target = "packageName", source = "servicePackage.packageName")
    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "productName", source = "product.productName")
    @Mapping(target = "productCode", source = "product.sku")
    ServicePackageProductDto toServicePackageProductDto(ServicePackageProduct servicePackageProduct);
    
    List<ServicePackageProductDto> toServicePackageProductDtoList(List<ServicePackageProduct> servicePackageProducts);
    
    @Mapping(target = "servicePackageProductId", ignore = true)
    @Mapping(target = "servicePackage", ignore = true) // Will be set in service
    @Mapping(target = "product", ignore = true) // Will be set in service
    @Mapping(target = "totalPrice", ignore = true) // Will be calculated
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isDeleted", constant = "false")
    ServicePackageProduct toEntity(CreateServicePackageProductRequest createRequest);
    
    default ServicePackageProduct updateEntity(ServicePackageProduct existingProduct, UpdateServicePackageProductRequest updateRequest) {
        if (updateRequest == null) {
            return existingProduct;
        }
        
        if (updateRequest.getQuantity() != null) {
            existingProduct.setQuantity(updateRequest.getQuantity());
        }
        if (updateRequest.getUnitPrice() != null) {
            existingProduct.setUnitPrice(updateRequest.getUnitPrice());
        }
        if (updateRequest.getNotes() != null) {
            existingProduct.setNotes(updateRequest.getNotes());
        }
        if (updateRequest.getIsRequired() != null) {
            existingProduct.setIsRequired(updateRequest.getIsRequired());
        }
        if (updateRequest.getIsActive() != null) {
            existingProduct.setIsActive(updateRequest.getIsActive());
        }
        
        // Recalculate total price if quantity or unit price changed
        if (updateRequest.getQuantity() != null || updateRequest.getUnitPrice() != null) {
            existingProduct.updateTotalPrice();
        }
        
        return existingProduct;
    }
}
