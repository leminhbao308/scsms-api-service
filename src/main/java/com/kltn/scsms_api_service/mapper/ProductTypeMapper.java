package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.productTypeManagement.ProductTypeInfoDto;
import com.kltn.scsms_api_service.core.dto.productTypeManagement.param.ProductTypeFilterParam;
import com.kltn.scsms_api_service.core.dto.productTypeManagement.request.CreateProductTypeRequest;
import com.kltn.scsms_api_service.core.dto.productTypeManagement.request.UpdateProductTypeRequest;
import com.kltn.scsms_api_service.core.entity.ProductType;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { AuditMapper.class })
public interface ProductTypeMapper {
    
    // ===== ENTITY TO DTO MAPPINGS =====
    
    /**
     * Map ProductType entity to ProductTypeInfoDto
     */
    @Mapping(target = "categoryId", source = "category.categoryId")
    @Mapping(target = "categoryName", source = "category.categoryName")
    ProductTypeInfoDto toInfoDto(ProductType productType);
    
    /**
     * Map list of ProductType entities to list of ProductTypeInfoDto
     */
    List<ProductTypeInfoDto> toInfoDtoList(List<ProductType> productTypes);
    
    // ===== DTO TO ENTITY MAPPINGS =====
    
    /**
     * Map CreateProductTypeRequest to ProductType entity
     */
    @Mapping(target = "productTypeId", ignore = true)
    @Mapping(target = "category", ignore = true) // Will be handled by service
    ProductType toEntity(CreateProductTypeRequest createRequest);
    
    /**
     * Update existing ProductType entity from UpdateProductTypeRequest
     */
    @Mapping(target = "productTypeId", ignore = true)
    @Mapping(target = "category", ignore = true) // Will be handled by service
    @Mapping(target = "isActive", ignore = true) // isActive is not updated via this method
    @Mapping(target = "isDeleted", ignore = true) // isDeleted is not updated via this method
    void updateEntityFromRequest(UpdateProductTypeRequest updateRequest, @MappingTarget ProductType productType);
    
    // ===== FILTER MAPPINGS =====
    
    /**
     * Map ProductTypeFilterParam to Specification (if needed)
     */
    default ProductTypeFilterParam toFilterParam(ProductTypeFilterParam filterParam) {
        return filterParam;
    }
}
