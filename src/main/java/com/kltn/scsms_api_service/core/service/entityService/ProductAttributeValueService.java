package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.Product;
import com.kltn.scsms_api_service.core.entity.ProductAttribute;
import com.kltn.scsms_api_service.core.entity.ProductAttributeValue;
import com.kltn.scsms_api_service.core.entity.compositId.ProductAttributeValueId;
import com.kltn.scsms_api_service.core.repository.ProductAttributeValueRepository;
import com.kltn.scsms_api_service.core.repository.ProductRepository;
import com.kltn.scsms_api_service.core.repository.ProductAttributeRepository;
import com.kltn.scsms_api_service.exception.ServerSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductAttributeValueService {

    private final ProductAttributeValueRepository productAttributeValueRepository;
    private final ProductRepository productRepository;
    private final ProductAttributeRepository productAttributeRepository;

    @Transactional
    public ProductAttributeValue createProductAttributeValue(UUID productId, UUID attributeId, String valueText, java.math.BigDecimal valueNumber) {
        log.info("Creating product attribute value for product: {} and attribute: {}", productId, attributeId);
        
        // Validate product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ServerSideException(ErrorCode.ENTITY_NOT_FOUND, "Product not found with ID: " + productId));
        
        // Validate attribute exists
        ProductAttribute attribute = productAttributeRepository.findById(attributeId)
                .orElseThrow(() -> new ServerSideException(ErrorCode.ENTITY_NOT_FOUND, "Product attribute not found with ID: " + attributeId));
        
        // Check if combination already exists
        ProductAttributeValueId id = new ProductAttributeValueId(productId, attributeId);
        if (productAttributeValueRepository.existsById(id)) {
            throw new ServerSideException(ErrorCode.INVALID_INPUT, "Product attribute value already exists for product: " + productId + " and attribute: " + attributeId);
        }
        
        // Create new attribute value
        ProductAttributeValue attributeValue = ProductAttributeValue.builder()
                .product(product)
                .productAttribute(attribute)
                .valueText(valueText)
                .valueNumber(valueNumber)
                .isActive(true)
                .isDeleted(false)
                .build();
        
        return productAttributeValueRepository.save(attributeValue);
    }

    @Transactional(readOnly = true)
    public Optional<ProductAttributeValue> getProductAttributeValue(UUID productId, UUID attributeId) {
        log.info("Getting product attribute value for product: {} and attribute: {}", productId, attributeId);
        ProductAttributeValueId id = new ProductAttributeValueId(productId, attributeId);
        return productAttributeValueRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<ProductAttributeValue> getProductAttributeValues(UUID productId) {
        log.info("Getting all attribute values for product: {}", productId);
        return productAttributeValueRepository.findByProductIdAndIsDeletedFalse(productId);
    }

    @Transactional(readOnly = true)
    public List<ProductAttributeValue> getProductsByAttribute(UUID attributeId) {
        log.info("Getting all products for attribute: {}", attributeId);
        return productAttributeValueRepository.findByAttributeIdAndIsDeletedFalse(attributeId);
    }

    @Transactional
    public ProductAttributeValue updateProductAttributeValue(UUID productId, UUID attributeId, String valueText, java.math.BigDecimal valueNumber) {
        log.info("Updating product attribute value for product: {} and attribute: {}", productId, attributeId);
        
        ProductAttributeValueId id = new ProductAttributeValueId(productId, attributeId);
        ProductAttributeValue attributeValue = productAttributeValueRepository.findById(id)
                .orElseThrow(() -> new ServerSideException(ErrorCode.ENTITY_NOT_FOUND, "Product attribute value not found for product: " + productId + " and attribute: " + attributeId));
        
        attributeValue.setValueText(valueText);
        attributeValue.setValueNumber(valueNumber);
        
        return productAttributeValueRepository.save(attributeValue);
    }

    @Transactional
    public ProductAttributeValue save(ProductAttributeValue attributeValue) {
        log.debug("Saving product attribute value: {}", attributeValue);
        return productAttributeValueRepository.save(attributeValue);
    }

    @Transactional
    public ProductAttributeValue update(ProductAttributeValue attributeValue) {
        log.debug("Updating product attribute value: {}", attributeValue);
        return productAttributeValueRepository.save(attributeValue);
    }

    @Transactional
    public void deleteProductAttributeValue(UUID productId, UUID attributeId) {
        log.info("Deleting product attribute value for product: {} and attribute: {}", productId, attributeId);
        
        ProductAttributeValueId id = new ProductAttributeValueId(productId, attributeId);
        ProductAttributeValue attributeValue = productAttributeValueRepository.findById(id)
                .orElseThrow(() -> new ServerSideException(ErrorCode.ENTITY_NOT_FOUND, "Product attribute value not found for product: " + productId + " and attribute: " + attributeId));
        
        attributeValue.setIsDeleted(true);
        attributeValue.setIsActive(false);
        productAttributeValueRepository.save(attributeValue);
    }

    @Transactional
    public List<ProductAttributeValue> bulkUpdateProductAttributeValues(List<UUID> productIds, UUID attributeId, String valueText, java.math.BigDecimal valueNumber) {
        log.info("Bulk updating attribute values for {} products and attribute: {}", productIds.size(), attributeId);
        
        List<ProductAttributeValue> updatedValues = new java.util.ArrayList<>();
        
        for (UUID productId : productIds) {
            try {
                ProductAttributeValueId id = new ProductAttributeValueId(productId, attributeId);
                Optional<ProductAttributeValue> existingValue = productAttributeValueRepository.findById(id);
                
                if (existingValue.isPresent()) {
                    // Update existing value
                    ProductAttributeValue attributeValue = existingValue.get();
                    attributeValue.setValueText(valueText);
                    attributeValue.setValueNumber(valueNumber);
                    updatedValues.add(productAttributeValueRepository.save(attributeValue));
                } else {
                    // Create new value
                    ProductAttributeValue newValue = createProductAttributeValue(productId, attributeId, valueText, valueNumber);
                    updatedValues.add(newValue);
                }
            } catch (Exception e) {
                log.warn("Failed to update attribute value for product: {} and attribute: {}", productId, attributeId, e);
            }
        }
        
        return updatedValues;
    }

    @Transactional(readOnly = true)
    public List<ProductAttributeValue> searchProductsByAttributeValue(UUID attributeId, String value) {
        log.info("Searching products by attribute value for attribute: {} and value: {}", attributeId, value);
        return productAttributeValueRepository.findByAttributeIdAndValueTextContainingIgnoreCaseAndIsDeletedFalse(attributeId, value);
    }

    @Transactional(readOnly = true)
    public List<ProductAttributeValue> searchProductsByAttributeValueRange(UUID attributeId, java.math.BigDecimal minValue, java.math.BigDecimal maxValue) {
        log.info("Searching products by attribute value range for attribute: {} between {} and {}", attributeId, minValue, maxValue);
        return productAttributeValueRepository.findByAttributeIdAndValueNumberBetweenAndIsDeletedFalse(attributeId, minValue, maxValue);
    }

    @Transactional(readOnly = true)
    public long countProductAttributeValues(UUID productId) {
        return productAttributeValueRepository.countByProductIdAndIsDeletedFalse(productId);
    }

    @Transactional(readOnly = true)
    public long countProductsByAttribute(UUID attributeId) {
        return productAttributeValueRepository.countByAttributeIdAndIsDeletedFalse(attributeId);
    }
}
