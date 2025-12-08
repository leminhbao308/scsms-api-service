package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.productTypeManagement.ProductTypeInfoDto;
import com.kltn.scsms_api_service.core.dto.productTypeManagement.param.ProductTypeFilterParam;
import com.kltn.scsms_api_service.core.dto.productTypeManagement.request.CreateProductTypeRequest;
import com.kltn.scsms_api_service.core.dto.productTypeManagement.request.UpdateProductTypeRequest;
import com.kltn.scsms_api_service.core.dto.productTypeManagement.request.UpdateProductTypeStatusRequest;
import com.kltn.scsms_api_service.core.entity.Category;
import com.kltn.scsms_api_service.core.entity.ProductType;
import com.kltn.scsms_api_service.core.service.entityService.ProductTypeService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.ProductTypeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductTypeManagementService {
    
    private final ProductTypeService productTypeService;
    private final ProductTypeMapper productTypeMapper;
    
    /**
     * Get all product types with pagination and filtering
     */
    public Page<ProductTypeInfoDto> getAllProductTypes(ProductTypeFilterParam filterParam) {
        log.debug("Fetching product types with filter: {}", filterParam);
        return productTypeService.getAllProductTypes(filterParam);
    }
    
    /**
     * Get product type by ID
     */
    public ProductTypeInfoDto getProductTypeById(UUID productTypeId) {
        log.debug("Fetching product type by ID: {}", productTypeId);
        return productTypeService.getProductTypeById(productTypeId);
    }
    
    /**
     * Get product type by code
     */
    public ProductTypeInfoDto getProductTypeByCode(String productTypeCode) {
        log.debug("Fetching product type by code: {}", productTypeCode);
        return productTypeService.getProductTypeByCode(productTypeCode);
    }
    
    /**
     * Get product types by category
     */
    public List<ProductTypeInfoDto> getProductTypesByCategory(UUID categoryId) {
        log.debug("Fetching product types by category ID: {}", categoryId);
        return productTypeService.getProductTypesByCategory(categoryId);
    }
    
    /**
     * Get active product types
     */
    public List<ProductTypeInfoDto> getActiveProductTypes() {
        log.debug("Fetching active product types");
        return productTypeService.getActiveProductTypes();
    }
    
    /**
     * Create new product type
     */
    @Transactional
    public ProductTypeInfoDto createProductType(CreateProductTypeRequest createRequest) {
        log.info("Creating product type: {}", createRequest.getProductTypeName());
        
        // Validate request
        validateProductTypeCreateRequest(createRequest);
        
        // Check if product type code is unique
        if (!productTypeService.isProductTypeCodeUnique(createRequest.getProductTypeCode())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                "Product type code '" + createRequest.getProductTypeCode() + "' already exists");
        }
        
        // Validate category exists
        Category category = productTypeService.findCategoryById(createRequest.getCategoryId())
            .orElseThrow(() -> new ClientSideException(ErrorCode.BAD_REQUEST, 
                "Category not found with ID: " + createRequest.getCategoryId()));
        
        // Create product type entity
        ProductType productType = productTypeMapper.toEntity(createRequest);
        productType.setCategory(category);
        
        return productTypeService.createProductType(productType);
    }
    
    /**
     * Update existing product type
     */
    @Transactional
    public ProductTypeInfoDto updateProductType(UUID productTypeId, UpdateProductTypeRequest updateRequest) {
        log.info("Updating product type: {}", productTypeId);
        
        // Validate request
        validateProductTypeUpdateRequest(updateRequest);
        
        // Find existing product type
        ProductType existingProductType = productTypeService.findById(productTypeId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, 
                "Product type not found with ID: " + productTypeId));
        
        // Check if product type code is unique (if being updated)
        if (updateRequest.getProductTypeCode() != null && 
            !updateRequest.getProductTypeCode().equals(existingProductType.getProductTypeCode())) {
            if (!productTypeService.isProductTypeCodeUnique(updateRequest.getProductTypeCode(), productTypeId)) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Product type code '" + updateRequest.getProductTypeCode() + "' already exists");
            }
        }
        
        // Validate category exists (if being updated)
        if (updateRequest.getCategoryId() != null) {
            Category category = productTypeService.findCategoryById(updateRequest.getCategoryId())
                .orElseThrow(() -> new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Category not found with ID: " + updateRequest.getCategoryId()));
            existingProductType.setCategory(category);
        }
        
        // Update product type
        productTypeMapper.updateEntityFromRequest(updateRequest, existingProductType);
        
        return productTypeService.updateProductType(existingProductType);
    }
    
    /**
     * Delete product type (soft delete)
     */
    @Transactional
    public void deleteProductType(UUID productTypeId) {
        log.info("Soft deleting product type: {}", productTypeId);
        
        // Check if product type exists
        if (!productTypeService.findById(productTypeId).isPresent()) {
            throw new ClientSideException(ErrorCode.NOT_FOUND, 
                "Product type not found with ID: " + productTypeId);
        }
        
        productTypeService.deleteProductType(productTypeId);
    }
    
    /**
     * Update product type status
     */
    @Transactional
    public ProductTypeInfoDto updateProductTypeStatus(UUID productTypeId, UpdateProductTypeStatusRequest statusRequest) {
        log.info("Updating product type status: {} to {}", productTypeId, statusRequest.getIsActive());
        
        // Check if product type exists
        if (!productTypeService.findById(productTypeId).isPresent()) {
            throw new ClientSideException(ErrorCode.NOT_FOUND, 
                "Product type not found with ID: " + productTypeId);
        }
        
        return productTypeService.updateProductTypeStatus(productTypeId, statusRequest.getIsActive());
    }
    
    /**
     * Validate product type code
     */
    public boolean validateProductTypeCode(String productTypeCode) {
        if (productTypeCode == null || productTypeCode.trim().isEmpty()) {
            return false;
        }
        return productTypeService.isProductTypeCodeUnique(productTypeCode);
    }
    
    /**
     * Get product type statistics
     */
    public ProductTypeStatsDto getProductTypeStatistics() {
        log.debug("Fetching product type statistics");
        
        long totalProductTypes = productTypeService.getTotalProductTypesCount();
        long activeProductTypes = productTypeService.getActiveProductTypesCount();
        
        return ProductTypeStatsDto.builder()
            .totalProductTypes(totalProductTypes)
            .activeProductTypes(activeProductTypes)
            .inactiveProductTypes(totalProductTypes - activeProductTypes)
            .build();
    }
    
    // ===== PRIVATE VALIDATION METHODS =====
    
    private void validateProductTypeCreateRequest(CreateProductTypeRequest request) {
        if (request == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Request cannot be null");
        }
        
        if (request.getProductTypeName() == null || request.getProductTypeName().trim().isEmpty()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Product type name is required");
        }
        
        if (request.getProductTypeCode() == null || request.getProductTypeCode().trim().isEmpty()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Product type code is required");
        }
        
        if (request.getCategoryId() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Category ID is required");
        }
    }
    
    private void validateProductTypeUpdateRequest(UpdateProductTypeRequest request) {
        if (request == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Request cannot be null");
        }
        
        // At least one field should be provided for update
        if (request.getProductTypeName() == null && 
            request.getProductTypeCode() == null && 
            request.getDescription() == null && 
            request.getCategoryId() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "At least one field must be provided for update");
        }
    }
    
    // ===== STATISTICS DTO =====
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProductTypeStatsDto {
        private long totalProductTypes;
        private long activeProductTypes;
        private long inactiveProductTypes;
    }
}
