package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.dto.productAttributeManagement.ProductAttributeInfoDto;
import com.kltn.scsms_api_service.core.dto.productAttributeManagement.param.ProductAttributeFilterParam;
import com.kltn.scsms_api_service.core.entity.ProductAttribute;
import com.kltn.scsms_api_service.core.repository.ProductAttributeRepository;
import com.kltn.scsms_api_service.mapper.ProductAttributeMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductAttributeService {
    
    private final ProductAttributeRepository productAttributeRepository;
    private final ProductAttributeMapper productAttributeMapper;
    private final EntityManager entityManager;
    
    /**
     * Get all product attributes with pagination and filtering
     */
    public Page<ProductAttributeInfoDto> getAllProductAttributes(ProductAttributeFilterParam filterParam) {
        log.debug("Fetching product attributes with filter: {}", filterParam);
        
        // Create criteria query
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductAttribute> query = cb.createQuery(ProductAttribute.class);
        Root<ProductAttribute> root = query.from(ProductAttribute.class);
        
        // Build predicates
        List<Predicate> predicates = new ArrayList<>();
        
        // Filter by attribute name
        if (filterParam.getAttributeName() != null && !filterParam.getAttributeName().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("attributeName")), 
                "%" + filterParam.getAttributeName().toLowerCase() + "%"));
        }
        
        // Filter by attribute code
        if (filterParam.getAttributeCode() != null && !filterParam.getAttributeCode().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("attributeCode")), 
                "%" + filterParam.getAttributeCode().toLowerCase() + "%"));
        }
        
        // Filter by data type
        if (filterParam.getDataType() != null && !filterParam.getDataType().trim().isEmpty()) {
            try {
                ProductAttribute.DataType dataType = ProductAttribute.DataType.valueOf(filterParam.getDataType().toUpperCase());
                predicates.add(cb.equal(root.get("dataType"), dataType));
            } catch (IllegalArgumentException e) {
                // Invalid data type, return empty result
                return new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);
            }
        }
        
        // Filter by is required
        if (filterParam.getIsRequired() != null) {
            predicates.add(cb.equal(root.get("isRequired"), filterParam.getIsRequired()));
        }
        
        // Filter by is active
        if (filterParam.getIsActive() != null) {
            predicates.add(cb.equal(root.get("isActive"), filterParam.getIsActive()));
        } else {
            // Default: exclude deleted records
            predicates.add(cb.equal(root.get("isDeleted"), false));
        }
        
        // Apply predicates
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        // Apply sorting
        Sort sort = Sort.by(Sort.Direction.ASC, "attributeName");
        if (filterParam.getSort() != null && !filterParam.getSort().trim().isEmpty()) {
            Sort.Direction direction = filterParam.getDirection() != null && 
                filterParam.getDirection().equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(direction, filterParam.getSort());
        }
        
        // Apply pagination
        PageRequest pageRequest = PageRequest.of(
            filterParam.getPage(),
            filterParam.getSize(),
            sort
        );
        
        // Execute count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ProductAttribute> countRoot = countQuery.from(ProductAttribute.class);
        countQuery.select(cb.count(countRoot));
        
        List<Predicate> countPredicates = new ArrayList<>();
        if (filterParam.getAttributeName() != null && !filterParam.getAttributeName().trim().isEmpty()) {
            countPredicates.add(cb.like(cb.lower(countRoot.get("attributeName")), 
                "%" + filterParam.getAttributeName().toLowerCase() + "%"));
        }
        if (filterParam.getAttributeCode() != null && !filterParam.getAttributeCode().trim().isEmpty()) {
            countPredicates.add(cb.like(cb.lower(countRoot.get("attributeCode")), 
                "%" + filterParam.getAttributeCode().toLowerCase() + "%"));
        }
        if (filterParam.getDataType() != null && !filterParam.getDataType().trim().isEmpty()) {
            try {
                ProductAttribute.DataType dataType = ProductAttribute.DataType.valueOf(filterParam.getDataType().toUpperCase());
                countPredicates.add(cb.equal(countRoot.get("dataType"), dataType));
            } catch (IllegalArgumentException e) {
                return new PageImpl<>(new ArrayList<>(), pageRequest, 0);
            }
        }
        if (filterParam.getIsRequired() != null) {
            countPredicates.add(cb.equal(countRoot.get("isRequired"), filterParam.getIsRequired()));
        }
        if (filterParam.getIsActive() != null) {
            countPredicates.add(cb.equal(countRoot.get("isActive"), filterParam.getIsActive()));
        } else {
            countPredicates.add(cb.equal(countRoot.get("isDeleted"), false));
        }
        
        if (!countPredicates.isEmpty()) {
            countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        }
        
        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();
        
        // Execute main query with pagination
        TypedQuery<ProductAttribute> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageRequest.getOffset());
        typedQuery.setMaxResults(pageRequest.getPageSize());
        
        List<ProductAttribute> productAttributes = typedQuery.getResultList();
        List<ProductAttributeInfoDto> productAttributeDtos = productAttributeMapper.toInfoDtoList(productAttributes);
        
        return new PageImpl<>(productAttributeDtos, pageRequest, totalElements);
    }
    
    /**
     * Get product attribute by ID
     */
    public ProductAttributeInfoDto getProductAttributeById(UUID attributeId) {
        log.debug("Fetching product attribute by ID: {}", attributeId);
        
        ProductAttribute productAttribute = productAttributeRepository.findById(attributeId)
            .orElseThrow(() -> new EntityNotFoundException("Product attribute not found with ID: " + attributeId));
        
        if (productAttribute.getIsDeleted()) {
            throw new EntityNotFoundException("Product attribute not found with ID: " + attributeId);
        }
        
        return productAttributeMapper.toInfoDto(productAttribute);
    }
    
    /**
     * Get product attribute by code
     */
    public ProductAttributeInfoDto getProductAttributeByCode(String attributeCode) {
        log.debug("Fetching product attribute by code: {}", attributeCode);
        
        ProductAttribute productAttribute = productAttributeRepository.findByAttributeCode(attributeCode)
            .orElseThrow(() -> new EntityNotFoundException("Product attribute not found with code: " + attributeCode));
        
        if (productAttribute.getIsDeleted()) {
            throw new EntityNotFoundException("Product attribute not found with code: " + attributeCode);
        }
        
        return productAttributeMapper.toInfoDto(productAttribute);
    }
    
    /**
     * Get product attributes by data type
     */
    public List<ProductAttributeInfoDto> getProductAttributesByDataType(ProductAttribute.DataType dataType) {
        log.debug("Fetching product attributes by data type: {}", dataType);
        
        List<ProductAttribute> productAttributes = productAttributeRepository.findByDataType(dataType);
        return productAttributeMapper.toInfoDtoList(productAttributes);
    }
    
    /**
     * Get required product attributes
     */
    public List<ProductAttributeInfoDto> getRequiredProductAttributes() {
        log.debug("Fetching required product attributes");
        
        List<ProductAttribute> productAttributes = productAttributeRepository.findByIsRequiredTrue();
        return productAttributeMapper.toInfoDtoList(productAttributes);
    }
    
    /**
     * Get active product attributes
     */
    public List<ProductAttributeInfoDto> getActiveProductAttributes() {
        log.debug("Fetching active product attributes");
        
        List<ProductAttribute> productAttributes = productAttributeRepository.findByIsActiveTrue();
        return productAttributeMapper.toInfoDtoList(productAttributes);
    }
    
    /**
     * Create new product attribute
     */
    @Transactional
    public ProductAttributeInfoDto createProductAttribute(ProductAttribute productAttribute) {
        log.debug("Creating product attribute: {}", productAttribute.getAttributeName());
        
        // Ensure audit fields are set
        if (productAttribute.getIsActive() == null) {
            productAttribute.setIsActive(true);
        }
        if (productAttribute.getIsDeleted() == null) {
            productAttribute.setIsDeleted(false);
        }
        
        ProductAttribute savedProductAttribute = productAttributeRepository.save(productAttribute);
        return productAttributeMapper.toInfoDto(savedProductAttribute);
    }
    
    /**
     * Update existing product attribute
     */
    @Transactional
    public ProductAttributeInfoDto updateProductAttribute(ProductAttribute productAttribute) {
        log.debug("Updating product attribute: {}", productAttribute.getAttributeId());
        
        // Ensure audit fields are set
        if (productAttribute.getIsActive() == null) {
            productAttribute.setIsActive(true);
        }
        if (productAttribute.getIsDeleted() == null) {
            productAttribute.setIsDeleted(false);
        }
        
        ProductAttribute savedProductAttribute = productAttributeRepository.save(productAttribute);
        return productAttributeMapper.toInfoDto(savedProductAttribute);
    }
    
    /**
     * Soft delete product attribute
     */
    @Transactional
    public void deleteProductAttribute(UUID attributeId) {
        log.debug("Soft deleting product attribute: {}", attributeId);
        
        ProductAttribute productAttribute = productAttributeRepository.findById(attributeId)
            .orElseThrow(() -> new EntityNotFoundException("Product attribute not found with ID: " + attributeId));
        
        productAttribute.setIsDeleted(true);
        productAttribute.setIsActive(false);
        productAttributeRepository.save(productAttribute);
    }
    
    /**
     * Update product attribute status
     */
    @Transactional
    public ProductAttributeInfoDto updateProductAttributeStatus(UUID attributeId, Boolean isActive) {
        log.debug("Updating product attribute status: {} to {}", attributeId, isActive);
        
        ProductAttribute productAttribute = productAttributeRepository.findById(attributeId)
            .orElseThrow(() -> new EntityNotFoundException("Product attribute not found with ID: " + attributeId));
        
        productAttribute.setIsActive(isActive);
        ProductAttribute savedProductAttribute = productAttributeRepository.save(productAttribute);
        return productAttributeMapper.toInfoDto(savedProductAttribute);
    }
    
    /**
     * Check if attribute code is unique
     */
    public boolean isAttributeCodeUnique(String attributeCode) {
        return !productAttributeRepository.existsByAttributeCode(attributeCode);
    }
    
    /**
     * Check if attribute code is unique, excluding specific attribute ID
     */
    public boolean isAttributeCodeUnique(String attributeCode, UUID attributeId) {
        return !productAttributeRepository.existsByAttributeCodeAndAttributeIdNot(attributeCode, attributeId);
    }
    
    /**
     * Find product attribute entity by ID
     */
    public Optional<ProductAttribute> findById(UUID attributeId) {
        return productAttributeRepository.findById(attributeId);
    }
    
    /**
     * Get product attribute statistics
     */
    public long getTotalProductAttributesCount() {
        return productAttributeRepository.getTotalProductAttributesCount();
    }
    
    public long getActiveProductAttributesCount() {
        return productAttributeRepository.getActiveProductAttributesCount();
    }
    
    public long getProductAttributesCountByDataType(ProductAttribute.DataType dataType) {
        return productAttributeRepository.countByDataType(dataType);
    }
    
    public long getRequiredProductAttributesCount() {
        return productAttributeRepository.countByIsRequiredTrue();
    }
}
