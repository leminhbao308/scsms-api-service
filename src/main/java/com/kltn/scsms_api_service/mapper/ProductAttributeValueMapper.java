package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.productManagement.ProductAttributeValueDto;
import com.kltn.scsms_api_service.core.dto.productManagement.request.ProductAttributeValueRequest;
import com.kltn.scsms_api_service.core.entity.ProductAttributeValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { AuditMapper.class })
public interface ProductAttributeValueMapper {

    // ===== ENTITY TO DTO MAPPINGS =====

    /**
     * Map ProductAttributeValue entity to ProductAttributeValueDto
     */
    @Mapping(target = "attributeName", source = "productAttribute.attributeName")
    @Mapping(target = "attributeCode", source = "productAttribute.attributeCode")
    @Mapping(target = "unit", source = "productAttribute.unit")
    @Mapping(target = "dataType", source = "productAttribute.dataType")
    @Mapping(target = "displayValue", expression = "java(productAttributeValue.getDisplayValue())")
    ProductAttributeValueDto toDto(ProductAttributeValue productAttributeValue);

    /**
     * Map list of ProductAttributeValue entities to list of ProductAttributeValueDto
     */
    List<ProductAttributeValueDto> toDtoList(List<ProductAttributeValue> productAttributeValues);

    // ===== DTO TO ENTITY MAPPINGS =====

    /**
     * Map ProductAttributeValueRequest to ProductAttributeValue entity
     */
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "productAttribute", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    ProductAttributeValue toEntity(ProductAttributeValueRequest request);

    /**
     * Update existing ProductAttributeValue entity from ProductAttributeValueRequest
     */
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "productAttribute", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "attributeId", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    void updateEntityFromRequest(ProductAttributeValueRequest request, @MappingTarget ProductAttributeValue productAttributeValue);
}
