package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.dto.productTypeManagement.ProductTypeInfoDto;
import com.kltn.scsms_api_service.core.dto.productTypeManagement.param.ProductTypeFilterParam;
import com.kltn.scsms_api_service.core.entity.Category;
import com.kltn.scsms_api_service.core.entity.ProductType;
import com.kltn.scsms_api_service.core.repository.CategoryRepository;
import com.kltn.scsms_api_service.core.repository.ProductTypeRepository;
import com.kltn.scsms_api_service.mapper.ProductTypeMapper;
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
public class ProductTypeService {
    
    private final ProductTypeRepository productTypeRepository;
    private final CategoryRepository categoryRepository;
    private final ProductTypeMapper productTypeMapper;
    private final EntityManager entityManager;
    
    /**
     * Get all product types with pagination and filtering
     */
    public Page<ProductTypeInfoDto> getAllProductTypes(ProductTypeFilterParam filterParam) {
        log.debug("Fetching product types with filter: {}", filterParam);
        
        // Create criteria query
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductType> query = cb.createQuery(ProductType.class);
        Root<ProductType> root = query.from(ProductType.class);
        
        // Join with category for filtering
        Join<ProductType, Category> categoryJoin = root.join("category", JoinType.LEFT);
        
        // Build predicates
        List<Predicate> predicates = new ArrayList<>();
        
        // Filter by product type name
        if (filterParam.getProductTypeName() != null && !filterParam.getProductTypeName().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("productTypeName")), 
                "%" + filterParam.getProductTypeName().toLowerCase() + "%"));
        }
        
        // Filter by product type code
        if (filterParam.getProductTypeCode() != null && !filterParam.getProductTypeCode().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("productTypeCode")), 
                "%" + filterParam.getProductTypeCode().toLowerCase() + "%"));
        }
        
        // Filter by category name
        if (filterParam.getCategoryName() != null && !filterParam.getCategoryName().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(categoryJoin.get("categoryName")), 
                "%" + filterParam.getCategoryName().toLowerCase() + "%"));
        }
        
        // Filter by active status
        if (filterParam.getIsActive() != null) {
            predicates.add(cb.equal(root.get("isActive"), filterParam.getIsActive()));
        }
        
        // Always filter out deleted records
        predicates.add(cb.equal(root.get("isDeleted"), false));
        
        // Apply predicates
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        // Apply sorting
        Sort sort = Sort.by(Sort.Direction.ASC, "productTypeName");
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
        Root<ProductType> countRoot = countQuery.from(ProductType.class);
        Join<ProductType, Category> countCategoryJoin = countRoot.join("category", JoinType.LEFT);
        
        countQuery.select(cb.count(countRoot));
        List<Predicate> countPredicates = new ArrayList<>();
        
        // Apply same filters for count
        if (filterParam.getProductTypeName() != null && !filterParam.getProductTypeName().trim().isEmpty()) {
            countPredicates.add(cb.like(cb.lower(countRoot.get("productTypeName")), 
                "%" + filterParam.getProductTypeName().toLowerCase() + "%"));
        }
        if (filterParam.getProductTypeCode() != null && !filterParam.getProductTypeCode().trim().isEmpty()) {
            countPredicates.add(cb.like(cb.lower(countRoot.get("productTypeCode")), 
                "%" + filterParam.getProductTypeCode().toLowerCase() + "%"));
        }
        if (filterParam.getCategoryName() != null && !filterParam.getCategoryName().trim().isEmpty()) {
            countPredicates.add(cb.like(cb.lower(countCategoryJoin.get("categoryName")), 
                "%" + filterParam.getCategoryName().toLowerCase() + "%"));
        }
        if (filterParam.getIsActive() != null) {
            countPredicates.add(cb.equal(countRoot.get("isActive"), filterParam.getIsActive()));
        }
        countPredicates.add(cb.equal(countRoot.get("isDeleted"), false));
        
        if (!countPredicates.isEmpty()) {
            countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        }
        
        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();
        
        // Execute main query with pagination
        TypedQuery<ProductType> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageRequest.getOffset());
        typedQuery.setMaxResults(pageRequest.getPageSize());
        
        List<ProductType> productTypes = typedQuery.getResultList();
        List<ProductTypeInfoDto> productTypeDtos = productTypeMapper.toInfoDtoList(productTypes);
        
        return new PageImpl<>(productTypeDtos, pageRequest, totalElements);
    }
    
    /**
     * Get product type by ID
     */
    public ProductTypeInfoDto getProductTypeById(UUID productTypeId) {
        log.debug("Fetching product type by ID: {}", productTypeId);
        
        ProductType productType = productTypeRepository.findById(productTypeId)
            .orElseThrow(() -> new EntityNotFoundException("Product type not found with ID: " + productTypeId));
        
        if (productType.getIsDeleted()) {
            throw new EntityNotFoundException("Product type not found with ID: " + productTypeId);
        }
        
        return productTypeMapper.toInfoDto(productType);
    }
    
    /**
     * Get product type by code
     */
    public ProductTypeInfoDto getProductTypeByCode(String productTypeCode) {
        log.debug("Fetching product type by code: {}", productTypeCode);
        
        ProductType productType = productTypeRepository.findByProductTypeCode(productTypeCode)
            .orElseThrow(() -> new EntityNotFoundException("Product type not found with code: " + productTypeCode));
        
        if (productType.getIsDeleted()) {
            throw new EntityNotFoundException("Product type not found with code: " + productTypeCode);
        }
        
        return productTypeMapper.toInfoDto(productType);
    }
    
    /**
     * Get product types by category
     */
    public List<ProductTypeInfoDto> getProductTypesByCategory(UUID categoryId) {
        log.debug("Fetching product types by category ID: {}", categoryId);
        
        List<ProductType> productTypes = productTypeRepository.findByCategoryCategoryId(categoryId);
        return productTypeMapper.toInfoDtoList(productTypes);
    }
    
    /**
     * Get active product types
     */
    public List<ProductTypeInfoDto> getActiveProductTypes() {
        log.debug("Fetching active product types");
        
        List<ProductType> productTypes = productTypeRepository.findByIsActiveTrue();
        return productTypeMapper.toInfoDtoList(productTypes);
    }
    
    /**
     * Create new product type
     */
    @Transactional
    public ProductTypeInfoDto createProductType(ProductType productType) {
        log.debug("Creating product type: {}", productType.getProductTypeName());
        
        // Ensure audit fields are set
        if (productType.getIsActive() == null) {
            productType.setIsActive(true);
        }
        if (productType.getIsDeleted() == null) {
            productType.setIsDeleted(false);
        }
        
        ProductType savedProductType = productTypeRepository.save(productType);
        return productTypeMapper.toInfoDto(savedProductType);
    }
    
    /**
     * Update existing product type
     */
    @Transactional
    public ProductTypeInfoDto updateProductType(ProductType productType) {
        log.debug("Updating product type: {}", productType.getProductTypeId());
        
        // Ensure audit fields are set
        if (productType.getIsActive() == null) {
            productType.setIsActive(true);
        }
        if (productType.getIsDeleted() == null) {
            productType.setIsDeleted(false);
        }
        
        ProductType savedProductType = productTypeRepository.save(productType);
        return productTypeMapper.toInfoDto(savedProductType);
    }
    
    /**
     * Soft delete product type
     */
    @Transactional
    public void deleteProductType(UUID productTypeId) {
        log.debug("Soft deleting product type: {}", productTypeId);
        
        ProductType productType = productTypeRepository.findById(productTypeId)
            .orElseThrow(() -> new EntityNotFoundException("Product type not found with ID: " + productTypeId));
        
        productType.setIsDeleted(true);
        productType.setIsActive(false);
        productTypeRepository.save(productType);
    }
    
    /**
     * Update product type status
     */
    @Transactional
    public ProductTypeInfoDto updateProductTypeStatus(UUID productTypeId, Boolean isActive) {
        log.debug("Updating product type status: {} to {}", productTypeId, isActive);
        
        ProductType productType = productTypeRepository.findById(productTypeId)
            .orElseThrow(() -> new EntityNotFoundException("Product type not found with ID: " + productTypeId));
        
        productType.setIsActive(isActive);
        ProductType savedProductType = productTypeRepository.save(productType);
        return productTypeMapper.toInfoDto(savedProductType);
    }
    
    /**
     * Check if product type code exists
     */
    public boolean isProductTypeCodeUnique(String productTypeCode) {
        return !productTypeRepository.existsByProductTypeCode(productTypeCode);
    }
    
    /**
     * Check if product type code exists, excluding specific product type ID
     */
    public boolean isProductTypeCodeUnique(String productTypeCode, UUID productTypeId) {
        return !productTypeRepository.existsByProductTypeCodeAndProductTypeIdNot(productTypeCode, productTypeId);
    }
    
    /**
     * Find product type entity by ID
     */
    public Optional<ProductType> findById(UUID productTypeId) {
        return productTypeRepository.findById(productTypeId);
    }
    
    /**
     * Find category entity by ID
     */
    public Optional<Category> findCategoryById(UUID categoryId) {
        return categoryRepository.findById(categoryId);
    }
    
    /**
     * Get product type statistics
     */
    public long getTotalProductTypesCount() {
        return productTypeRepository.getTotalProductTypesCount();
    }
    
    public long getActiveProductTypesCount() {
        return productTypeRepository.getActiveProductTypesCount();
    }
}
