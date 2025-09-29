package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.serviceManagement.ServiceProductDto;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.CreateServiceProductRequest;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.UpdateServiceProductRequest;
import com.kltn.scsms_api_service.core.entity.ServiceProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class}
)
public interface ServiceProductMapper {
    
    @Mapping(target = "serviceId", source = "service.serviceId")
    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "productName", source = "product.productName")
    @Mapping(target = "productUrl", source = "product.productUrl")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "productBrand", source = "product.brand")
    @Mapping(target = "productModel", source = "product.model")
    @Mapping(target = "unitOfMeasure", source = "product.unitOfMeasure")
    ServiceProductDto toServiceProductDto(ServiceProduct serviceProduct);
    
    @Mapping(target = "service", ignore = true) // Will be set in service
    @Mapping(target = "product", ignore = true) // Will be set in service
    @Mapping(target = "serviceProductId", ignore = true)
    @Mapping(target = "totalPrice", ignore = true) // Will be calculated
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isDeleted", constant = "false")
    ServiceProduct toEntity(CreateServiceProductRequest createServiceProductRequest);
    
    default ServiceProduct updateEntity(ServiceProduct existingServiceProduct, UpdateServiceProductRequest updateRequest) {
        if (updateRequest == null) {
            return existingServiceProduct;
        }
        
        if (updateRequest.getQuantity() != null) {
            existingServiceProduct.setQuantity(updateRequest.getQuantity());
        }
        if (updateRequest.getUnitPrice() != null) {
            existingServiceProduct.setUnitPrice(updateRequest.getUnitPrice());
        }
        if (updateRequest.getNotes() != null) {
            existingServiceProduct.setNotes(updateRequest.getNotes());
        }
        if (updateRequest.getIsRequired() != null) {
            existingServiceProduct.setIsRequired(updateRequest.getIsRequired());
        }
        if (updateRequest.getIsActive() != null) {
            existingServiceProduct.setIsActive(updateRequest.getIsActive());
        }
        
        // Update total price when quantity or unit price changes
        if (updateRequest.getQuantity() != null || updateRequest.getUnitPrice() != null) {
            existingServiceProduct.updateTotalPrice();
        }
        
        return existingServiceProduct;
    }
}
