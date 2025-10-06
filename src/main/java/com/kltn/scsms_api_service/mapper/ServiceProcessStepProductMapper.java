package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.ServiceProcessStepProductInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.request.CreateServiceProcessStepProductRequest;
import com.kltn.scsms_api_service.core.dto.serviceProcessManagement.request.UpdateServiceProcessStepProductRequest;
import com.kltn.scsms_api_service.core.entity.ServiceProcessStepProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ServiceProcessStepProductMapper {
    
    @Mapping(target = "stepId", source = "serviceProcessStep.id")
    @Mapping(target = "stepName", source = "serviceProcessStep.name")
    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "productName", source = "product.productName")
    @Mapping(target = "productCode", source = "product.sku")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "productCost", expression = "java(serviceProcessStepProduct.calculateProductCost())")
    ServiceProcessStepProductInfoDto toServiceProcessStepProductInfoDto(ServiceProcessStepProduct serviceProcessStepProduct);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "serviceProcessStep", ignore = true) // Will be set in service
    @Mapping(target = "product", ignore = true) // Will be set in service
    @Mapping(target = "isDeleted", constant = "false")
    ServiceProcessStepProduct toEntity(CreateServiceProcessStepProductRequest createServiceProcessStepProductRequest);
    
    default ServiceProcessStepProduct updateEntity(ServiceProcessStepProduct existingServiceProcessStepProduct, UpdateServiceProcessStepProductRequest updateRequest) {
        if (updateRequest == null) {
            return existingServiceProcessStepProduct;
        }
        
        if (updateRequest.getQuantity() != null) {
            existingServiceProcessStepProduct.setQuantity(updateRequest.getQuantity());
        }
        if (updateRequest.getUnit() != null) {
            existingServiceProcessStepProduct.setUnit(updateRequest.getUnit());
        }
        
        return existingServiceProcessStepProduct;
    }
}
