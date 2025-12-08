package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, UUID>, JpaSpecificationExecutor<ProductType> {
    
    /**
     * Find product type by code
     */
    Optional<ProductType> findByProductTypeCode(String productTypeCode);
    
    /**
     * Check if product type code exists
     */
    boolean existsByProductTypeCode(String productTypeCode);
    
    /**
     * Check if product type code exists, excluding specific product type ID
     */
    boolean existsByProductTypeCodeAndProductTypeIdNot(String productTypeCode, UUID productTypeId);
    
    /**
     * Find product types by category
     */
    List<ProductType> findByCategoryCategoryId(UUID categoryId);
    
    /**
     * Find product types by category with pagination
     */
    Page<ProductType> findByCategoryCategoryId(UUID categoryId, Pageable pageable);
    
    /**
     * Find product types by name (case-insensitive)
     */
    @Query("SELECT pt FROM ProductType pt WHERE LOWER(pt.productTypeName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<ProductType> findByProductTypeNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Find product types by name with pagination
     */
    @Query("SELECT pt FROM ProductType pt WHERE LOWER(pt.productTypeName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<ProductType> findByProductTypeNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    /**
     * Find active product types
     */
    List<ProductType> findByIsActiveTrue();
    
    /**
     * Find active product types with pagination
     */
    Page<ProductType> findByIsActiveTrue(Pageable pageable);
    
    /**
     * Find product types by category and active status
     */
    List<ProductType> findByCategoryCategoryIdAndIsActiveTrue(UUID categoryId);
    
    /**
     * Count product types by category
     */
    long countByCategoryCategoryId(UUID categoryId);
    
    /**
     * Count active product types by category
     */
    long countByCategoryCategoryIdAndIsActiveTrue(UUID categoryId);
    
    /**
     * Get total product types count
     */
    @Query("SELECT COUNT(pt) FROM ProductType pt")
    long getTotalProductTypesCount();
    
    /**
     * Get active product types count
     */
    @Query("SELECT COUNT(pt) FROM ProductType pt WHERE pt.isActive = true")
    long getActiveProductTypesCount();
}
