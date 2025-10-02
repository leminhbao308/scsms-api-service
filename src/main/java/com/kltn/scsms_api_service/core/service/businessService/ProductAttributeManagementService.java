package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.productAttributeManagement.ProductAttributeInfoDto;
import com.kltn.scsms_api_service.core.dto.productAttributeManagement.param.ProductAttributeFilterParam;
import com.kltn.scsms_api_service.core.dto.productAttributeManagement.request.CreateProductAttributeRequest;
import com.kltn.scsms_api_service.core.dto.productAttributeManagement.request.UpdateProductAttributeRequest;
import com.kltn.scsms_api_service.core.dto.productAttributeManagement.request.UpdateProductAttributeStatusRequest;
import com.kltn.scsms_api_service.core.entity.ProductAttribute;
import com.kltn.scsms_api_service.core.service.entityService.ProductAttributeService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.ProductAttributeMapper;
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
public class ProductAttributeManagementService {
    
    private final ProductAttributeService productAttributeService;
    private final ProductAttributeMapper productAttributeMapper;
    
    /**
     * Get all product attributes with pagination and filtering
     */
    public Page<ProductAttributeInfoDto> getAllProductAttributes(ProductAttributeFilterParam filterParam) {
        log.debug("Fetching product attributes with filter: {}", filterParam);
        return productAttributeService.getAllProductAttributes(filterParam);
    }
    
    /**
     * Get product attribute by ID
     */
    public ProductAttributeInfoDto getProductAttributeById(UUID attributeId) {
        log.debug("Fetching product attribute by ID: {}", attributeId);
        return productAttributeService.getProductAttributeById(attributeId);
    }
    
    /**
     * Get product attribute by code
     */
    public ProductAttributeInfoDto getProductAttributeByCode(String attributeCode) {
        log.debug("Fetching product attribute by code: {}", attributeCode);
        return productAttributeService.getProductAttributeByCode(attributeCode);
    }
    
    /**
     * Get product attributes by data type
     */
    public List<ProductAttributeInfoDto> getProductAttributesByDataType(String dataType) {
        log.debug("Fetching product attributes by data type: {}", dataType);
        
        ProductAttribute.DataType dataTypeEnum = parseDataType(dataType);
        return productAttributeService.getProductAttributesByDataType(dataTypeEnum);
    }
    
    /**
     * Get required product attributes
     */
    public List<ProductAttributeInfoDto> getRequiredProductAttributes() {
        log.debug("Fetching required product attributes");
        return productAttributeService.getRequiredProductAttributes();
    }
    
    /**
     * Get active product attributes
     */
    public List<ProductAttributeInfoDto> getActiveProductAttributes() {
        log.debug("Fetching active product attributes");
        return productAttributeService.getActiveProductAttributes();
    }
    
    /**
     * Create new product attribute
     */
    @Transactional
    public ProductAttributeInfoDto createProductAttribute(CreateProductAttributeRequest createRequest) {
        log.info("Creating product attribute: {}", createRequest.getAttributeName());
        
        // Validate request
        validateProductAttributeCreateRequest(createRequest);
        
        // Check if attribute code is unique
        if (!productAttributeService.isAttributeCodeUnique(createRequest.getAttributeCode())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                "Attribute code '" + createRequest.getAttributeCode() + "' already exists");
        }
        
        // Create product attribute entity
        ProductAttribute productAttribute = productAttributeMapper.toEntity(createRequest);
        
        return productAttributeService.createProductAttribute(productAttribute);
    }
    
    /**
     * Update existing product attribute
     */
    @Transactional
    public ProductAttributeInfoDto updateProductAttribute(UUID attributeId, UpdateProductAttributeRequest updateRequest) {
        log.info("Updating product attribute: {}", attributeId);
        
        // Validate request
        validateProductAttributeUpdateRequest(updateRequest);
        
        // Find existing product attribute
        ProductAttribute existingProductAttribute = productAttributeService.findById(attributeId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, 
                "Product attribute not found with ID: " + attributeId));
        
        // Check if attribute code is unique (if being updated)
        if (updateRequest.getAttributeCode() != null && 
            !updateRequest.getAttributeCode().equals(existingProductAttribute.getAttributeCode())) {
            if (!productAttributeService.isAttributeCodeUnique(updateRequest.getAttributeCode(), attributeId)) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Attribute code '" + updateRequest.getAttributeCode() + "' already exists");
            }
        }
        
        // Update product attribute
        productAttributeMapper.updateEntityFromRequest(updateRequest, existingProductAttribute);
        
        return productAttributeService.updateProductAttribute(existingProductAttribute);
    }
    
    /**
     * Delete product attribute (soft delete)
     */
    @Transactional
    public void deleteProductAttribute(UUID attributeId) {
        log.info("Soft deleting product attribute: {}", attributeId);
        
        // Check if product attribute exists
        if (!productAttributeService.findById(attributeId).isPresent()) {
            throw new ClientSideException(ErrorCode.NOT_FOUND, 
                "Product attribute not found with ID: " + attributeId);
        }
        
        productAttributeService.deleteProductAttribute(attributeId);
    }
    
    /**
     * Update product attribute status
     */
    @Transactional
    public ProductAttributeInfoDto updateProductAttributeStatus(UUID attributeId, UpdateProductAttributeStatusRequest statusRequest) {
        log.info("Updating product attribute status: {} to {}", attributeId, statusRequest.getIsActive());
        
        // Check if product attribute exists
        if (!productAttributeService.findById(attributeId).isPresent()) {
            throw new ClientSideException(ErrorCode.NOT_FOUND, 
                "Product attribute not found with ID: " + attributeId);
        }
        
        return productAttributeService.updateProductAttributeStatus(attributeId, statusRequest.getIsActive());
    }
    
    /**
     * Validate attribute code
     */
    public boolean validateAttributeCode(String attributeCode) {
        if (attributeCode == null || attributeCode.trim().isEmpty()) {
            return false;
        }
        return productAttributeService.isAttributeCodeUnique(attributeCode);
    }
    
    /**
     * Get product attribute statistics
     */
    public ProductAttributeStatsDto getProductAttributeStatistics() {
        log.debug("Fetching product attribute statistics");
        
        long totalAttributes = productAttributeService.getTotalProductAttributesCount();
        long activeAttributes = productAttributeService.getActiveProductAttributesCount();
        long requiredAttributes = productAttributeService.getRequiredProductAttributesCount();
        
        return ProductAttributeStatsDto.builder()
            .totalAttributes(totalAttributes)
            .activeAttributes(activeAttributes)
            .inactiveAttributes(totalAttributes - activeAttributes)
            .requiredAttributes(requiredAttributes)
            .stringTypeCount(productAttributeService.getProductAttributesCountByDataType(ProductAttribute.DataType.STRING))
            .numberTypeCount(productAttributeService.getProductAttributesCountByDataType(ProductAttribute.DataType.NUMBER))
            .booleanTypeCount(productAttributeService.getProductAttributesCountByDataType(ProductAttribute.DataType.BOOLEAN))
            .dateTypeCount(productAttributeService.getProductAttributesCountByDataType(ProductAttribute.DataType.DATE))
            .build();
    }
    
    // ===== PRIVATE VALIDATION METHODS =====
    
    private void validateProductAttributeCreateRequest(CreateProductAttributeRequest request) {
        if (request == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Request cannot be null");
        }
        
        if (request.getAttributeName() == null || request.getAttributeName().trim().isEmpty()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Attribute name is required");
        }
        
        if (request.getAttributeCode() == null || request.getAttributeCode().trim().isEmpty()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Attribute code is required");
        }
        
        // Validate data type if provided
        if (request.getDataType() != null && !request.getDataType().trim().isEmpty()) {
            try {
                ProductAttribute.DataType.valueOf(request.getDataType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Invalid data type: " + request.getDataType());
            }
        }
    }
    
    private void validateProductAttributeUpdateRequest(UpdateProductAttributeRequest request) {
        if (request == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Request cannot be null");
        }
        
        // At least one field should be provided for update
        if (request.getAttributeName() == null && 
            request.getAttributeCode() == null && 
            request.getUnit() == null && 
            request.getIsRequired() == null && 
            request.getDataType() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "At least one field must be provided for update");
        }
        
        // Validate data type if provided
        if (request.getDataType() != null && !request.getDataType().trim().isEmpty()) {
            try {
                ProductAttribute.DataType.valueOf(request.getDataType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Invalid data type: " + request.getDataType());
            }
        }
    }
    
    private ProductAttribute.DataType parseDataType(String dataType) {
        try {
            return ProductAttribute.DataType.valueOf(dataType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                "Invalid data type: " + dataType);
        }
    }
    
    // ===== STATISTICS DTO =====
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProductAttributeStatsDto {
        private long totalAttributes;
        private long activeAttributes;
        private long inactiveAttributes;
        private long requiredAttributes;
        private long stringTypeCount;
        private long numberTypeCount;
        private long booleanTypeCount;
        private long dateTypeCount;
    }
}
