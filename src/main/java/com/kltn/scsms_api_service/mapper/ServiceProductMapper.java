package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.serviceManagement.ServiceProductInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.CreateServiceProductRequest;
import com.kltn.scsms_api_service.core.dto.serviceManagement.request.UpdateServiceProductRequest;
import com.kltn.scsms_api_service.core.entity.ServiceProduct;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapper.class, ProductMapper.class})
public interface ServiceProductMapper {
    
    /**
     * Chuyển đổi từ ServiceProduct entity sang ServiceProductInfoDto
     */
    @Mapping(target = "serviceId", source = "service.serviceId")
    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "productInfo", source = "product")
    @Mapping(target = "audit.createdDate", source = "createdDate")
    @Mapping(target = "audit.modifiedDate", source = "modifiedDate")
    @Mapping(target = "audit.createdBy", source = "createdBy")
    @Mapping(target = "audit.modifiedBy", source = "modifiedBy")
    @Mapping(target = "audit.isActive", source = "isActive")
    @Mapping(target = "audit.isDeleted", source = "isDeleted")
    ServiceProductInfoDto toServiceProductInfoDto(ServiceProduct serviceProduct);
    
    /**
     * Chuyển đổi từ CreateServiceProductRequest sang ServiceProduct entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isDeleted", constant = "false")
    ServiceProduct toEntity(CreateServiceProductRequest createServiceProductRequest);
    
    /**
     * Cập nhật ServiceProduct entity từ UpdateServiceProductRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "product", ignore = true)
    void updateEntity(@MappingTarget ServiceProduct existingServiceProduct, UpdateServiceProductRequest updateServiceProductRequest);
}
