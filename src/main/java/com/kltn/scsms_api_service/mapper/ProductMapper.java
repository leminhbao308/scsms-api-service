package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.productManagement.ProductInfoDto;
import com.kltn.scsms_api_service.core.dto.productManagement.request.CreateProductRequest;
import com.kltn.scsms_api_service.core.dto.productManagement.request.UpdateProductRequest;
import com.kltn.scsms_api_service.core.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {AuditMapper.class, ProductTypeMapper.class, ProductAttributeValueMapper.class}
)
public interface ProductMapper {
    
    @Mapping(target = "productTypeId", source = "productType.productTypeId")
    @Mapping(target = "productTypeName", source = "productType.productTypeName")
    @Mapping(target = "attributeValues", source = "attributeValues")
    ProductInfoDto toProductInfoDto(Product product);
    
    @Mapping(target = "productType", ignore = true) // Will be set in service
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "attributeValues", ignore = true) // Will be handled separately
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isDeleted", constant = "false")
    Product toEntity(CreateProductRequest createProductRequest);
    
    @Mapping(target = "productType", ignore = true) // Will be handled by service
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "attributeValues", ignore = true) // Will be handled separately
    @Mapping(target = "isActive", source = "isActive", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntityFromRequest(UpdateProductRequest updateRequest, @MappingTarget Product product);
}