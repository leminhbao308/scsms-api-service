package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.productManagement.ProductAttributeValueDto;
import com.kltn.scsms_api_service.core.dto.productManagement.request.AddProductAttributeValueRequest;
import com.kltn.scsms_api_service.core.dto.productManagement.request.BulkUpdateProductAttributeValueRequest;
import com.kltn.scsms_api_service.core.dto.productManagement.request.UpdateProductAttributeValueRequest;
import com.kltn.scsms_api_service.core.entity.ProductAttributeValue;
import com.kltn.scsms_api_service.core.service.entityService.ProductAttributeValueService;
import com.kltn.scsms_api_service.exception.ServerSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.ProductAttributeValueMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductAttributeValueManagementService {

    private final ProductAttributeValueService productAttributeValueService;
    private final ProductAttributeValueMapper productAttributeValueMapper;

    @Transactional
    public ProductAttributeValueDto addProductAttributeValue(UUID productId, AddProductAttributeValueRequest request) {
        log.info("Adding attribute value for product: {} and attribute: {}", productId, request.getAttributeId());
        
        ProductAttributeValue attributeValue = productAttributeValueService.createProductAttributeValue(
                productId, 
                request.getAttributeId(), 
                request.getValueText(), 
                request.getValueNumber()
        );
        
        return productAttributeValueMapper.toDto(attributeValue);
    }

    @Transactional(readOnly = true)
    public ProductAttributeValueDto getProductAttributeValue(UUID productId, UUID attributeId) {
        log.info("Getting attribute value for product: {} and attribute: {}", productId, attributeId);
        
        ProductAttributeValue attributeValue = productAttributeValueService.getProductAttributeValue(productId, attributeId)
                .orElseThrow(() -> new ServerSideException(ErrorCode.ENTITY_NOT_FOUND, "Product attribute value not found for product: " + productId + " and attribute: " + attributeId));
        
        return productAttributeValueMapper.toDto(attributeValue);
    }

    @Transactional(readOnly = true)
    public List<ProductAttributeValueDto> getProductAttributeValues(UUID productId) {
        log.info("Getting all attribute values for product: {}", productId);
        
        List<ProductAttributeValue> attributeValues = productAttributeValueService.getProductAttributeValues(productId);
        return productAttributeValueMapper.toDtoList(attributeValues);
    }

    @Transactional(readOnly = true)
    public List<ProductAttributeValueDto> getProductsByAttribute(UUID attributeId) {
        log.info("Getting all products for attribute: {}", attributeId);
        
        List<ProductAttributeValue> attributeValues = productAttributeValueService.getProductsByAttribute(attributeId);
        return productAttributeValueMapper.toDtoList(attributeValues);
    }

    @Transactional
    public ProductAttributeValueDto updateProductAttributeValue(UUID productId, UUID attributeId, UpdateProductAttributeValueRequest request) {
        log.info("Updating attribute value for product: {} and attribute: {}", productId, attributeId);
        
        ProductAttributeValue attributeValue = productAttributeValueService.updateProductAttributeValue(
                productId, 
                attributeId, 
                request.getValueText(), 
                request.getValueNumber()
        );
        
        return productAttributeValueMapper.toDto(attributeValue);
    }

    @Transactional
    public void deleteProductAttributeValue(UUID productId, UUID attributeId) {
        log.info("Deleting attribute value for product: {} and attribute: {}", productId, attributeId);
        productAttributeValueService.deleteProductAttributeValue(productId, attributeId);
    }

    @Transactional
    public List<ProductAttributeValueDto> bulkUpdateProductAttributeValues(BulkUpdateProductAttributeValueRequest request) {
        log.info("Bulk updating attribute values for {} products and attribute: {}", request.getProductIds().size(), request.getAttributeId());
        
        List<ProductAttributeValue> updatedValues = productAttributeValueService.bulkUpdateProductAttributeValues(
                request.getProductIds(), 
                request.getAttributeId(), 
                request.getValueText(), 
                request.getValueNumber()
        );
        
        return productAttributeValueMapper.toDtoList(updatedValues);
    }

    @Transactional(readOnly = true)
    public List<ProductAttributeValueDto> searchProductsByAttributeValue(UUID attributeId, String value) {
        log.info("Searching products by attribute value for attribute: {} and value: {}", attributeId, value);
        
        List<ProductAttributeValue> attributeValues = productAttributeValueService.searchProductsByAttributeValue(attributeId, value);
        return productAttributeValueMapper.toDtoList(attributeValues);
    }

    @Transactional(readOnly = true)
    public List<ProductAttributeValueDto> searchProductsByAttributeValueRange(UUID attributeId, java.math.BigDecimal minValue, java.math.BigDecimal maxValue) {
        log.info("Searching products by attribute value range for attribute: {} between {} and {}", attributeId, minValue, maxValue);
        
        List<ProductAttributeValue> attributeValues = productAttributeValueService.searchProductsByAttributeValueRange(attributeId, minValue, maxValue);
        return productAttributeValueMapper.toDtoList(attributeValues);
    }

    @Transactional(readOnly = true)
    public long getProductAttributeValueCount(UUID productId) {
        return productAttributeValueService.countProductAttributeValues(productId);
    }

    @Transactional(readOnly = true)
    public long getProductCountByAttribute(UUID attributeId) {
        return productAttributeValueService.countProductsByAttribute(attributeId);
    }
}
