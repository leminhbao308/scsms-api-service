package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.productAttributeManagement.ProductAttributeInfoDto;
import com.kltn.scsms_api_service.core.dto.productAttributeManagement.param.ProductAttributeFilterParam;
import com.kltn.scsms_api_service.core.dto.productAttributeManagement.request.CreateProductAttributeRequest;
import com.kltn.scsms_api_service.core.dto.productAttributeManagement.request.UpdateProductAttributeRequest;
import com.kltn.scsms_api_service.core.entity.ProductAttribute;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductAttributeMapper {
    
    // ===== ENTITY TO DTO MAPPINGS =====
    
    /**
     * Map ProductAttribute entity to ProductAttributeInfoDto
     */
    @Mapping(target = "dataType", source = "dataType", qualifiedByName = "dataTypeToString")
    @Mapping(target = "displayName", source = ".", qualifiedByName = "generateDisplayName")
    ProductAttributeInfoDto toInfoDto(ProductAttribute productAttribute);
    
    /**
     * Map list of ProductAttribute entities to list of ProductAttributeInfoDto
     */
    List<ProductAttributeInfoDto> toInfoDtoList(List<ProductAttribute> productAttributes);
    
    // ===== DTO TO ENTITY MAPPINGS =====
    
    /**
     * Map CreateProductAttributeRequest to ProductAttribute entity
     */
    @Mapping(target = "attributeId", ignore = true)
    @Mapping(target = "dataType", source = "dataType", qualifiedByName = "stringToDataType")
    ProductAttribute toEntity(CreateProductAttributeRequest createRequest);
    
    /**
     * Update existing ProductAttribute entity from UpdateProductAttributeRequest
     */
    @Mapping(target = "attributeId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "dataType", source = "dataType", qualifiedByName = "stringToDataType")
    void updateEntityFromRequest(UpdateProductAttributeRequest updateRequest, @MappingTarget ProductAttribute productAttribute);
    
    // ===== ENUM CONVERSION METHODS =====
    
    @Named("dataTypeToString")
    default String dataTypeToString(ProductAttribute.DataType dataType) {
        return dataType != null ? dataType.name() : null;
    }
    
    @Named("stringToDataType")
    default ProductAttribute.DataType stringToDataType(String dataType) {
        if (dataType == null || dataType.trim().isEmpty()) {
            return ProductAttribute.DataType.STRING;
        }
        try {
            return ProductAttribute.DataType.valueOf(dataType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ProductAttribute.DataType.STRING;
        }
    }
    
    @Named("generateDisplayName")
    default String generateDisplayName(ProductAttribute productAttribute) {
        if (productAttribute == null) {
            return null;
        }
        return productAttribute.getDisplayName();
    }
    
    // ===== FILTER MAPPINGS =====
    
    /**
     * Map ProductAttributeFilterParam to Specification (if needed)
     */
    default ProductAttributeFilterParam toFilterParam(ProductAttributeFilterParam filterParam) {
        return filterParam;
    }
}
