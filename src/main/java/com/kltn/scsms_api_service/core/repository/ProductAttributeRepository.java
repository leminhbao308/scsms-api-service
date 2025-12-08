package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.ProductAttribute;
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
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, UUID>, JpaSpecificationExecutor<ProductAttribute> {
    
    /**
     * Find product attribute by attribute code
     */
    Optional<ProductAttribute> findByAttributeCode(String attributeCode);
    
    /**
     * Find product attributes by data type
     */
    List<ProductAttribute> findByDataType(ProductAttribute.DataType dataType);
    
    /**
     * Find product attributes by data type with pagination
     */
    Page<ProductAttribute> findByDataType(ProductAttribute.DataType dataType, Pageable pageable);
    
    /**
     * Find required product attributes
     */
    List<ProductAttribute> findByIsRequiredTrue();
    
    /**
     * Find required product attributes with pagination
     */
    Page<ProductAttribute> findByIsRequiredTrue(Pageable pageable);
    
    /**
     * Find active product attributes
     */
    List<ProductAttribute> findByIsActiveTrue();
    
    /**
     * Find active product attributes with pagination
     */
    Page<ProductAttribute> findByIsActiveTrue(Pageable pageable);
    
    /**
     * Find active and required product attributes
     */
    List<ProductAttribute> findByIsActiveTrueAndIsRequiredTrue();
    
    /**
     * Find product attributes by name containing (case insensitive)
     */
    List<ProductAttribute> findByAttributeNameContainingIgnoreCase(String attributeName);
    
    /**
     * Find product attributes by name containing with pagination
     */
    Page<ProductAttribute> findByAttributeNameContainingIgnoreCase(String attributeName, Pageable pageable);
    
    /**
     * Find product attributes by code containing (case insensitive)
     */
    List<ProductAttribute> findByAttributeCodeContainingIgnoreCase(String attributeCode);
    
    /**
     * Find product attributes by code containing with pagination
     */
    Page<ProductAttribute> findByAttributeCodeContainingIgnoreCase(String attributeCode, Pageable pageable);
    
    /**
     * Check if attribute code exists
     */
    boolean existsByAttributeCode(String attributeCode);
    
    /**
     * Check if attribute code exists, excluding specific attribute ID
     */
    boolean existsByAttributeCodeAndAttributeIdNot(String attributeCode, UUID attributeId);
    
    /**
     * Count product attributes by data type
     */
    long countByDataType(ProductAttribute.DataType dataType);
    
    /**
     * Count required product attributes
     */
    long countByIsRequiredTrue();
    
    /**
     * Count active product attributes
     */
    long countByIsActiveTrue();
    
    /**
     * Get total product attributes count
     */
    @Query("SELECT COUNT(pa) FROM ProductAttribute pa")
    long getTotalProductAttributesCount();
    
    /**
     * Get active product attributes count
     */
    @Query("SELECT COUNT(pa) FROM ProductAttribute pa WHERE pa.isActive = true")
    long getActiveProductAttributesCount();
    
    /**
     * Find product attributes by multiple data types
     */
    @Query("SELECT pa FROM ProductAttribute pa WHERE pa.dataType IN :dataTypes AND pa.isActive = true ORDER BY pa.attributeName")
    List<ProductAttribute> findByDataTypeIn(@Param("dataTypes") List<ProductAttribute.DataType> dataTypes);
    
    /**
     * Find product attributes with unit
     */
    @Query("SELECT pa FROM ProductAttribute pa WHERE pa.unit IS NOT NULL AND pa.unit != '' AND pa.isActive = true ORDER BY pa.attributeName")
    List<ProductAttribute> findAttributesWithUnit();
    
    /**
     * Find product attributes without unit
     */
    @Query("SELECT pa FROM ProductAttribute pa WHERE (pa.unit IS NULL OR pa.unit = '') AND pa.isActive = true ORDER BY pa.attributeName")
    List<ProductAttribute> findAttributesWithoutUnit();
    
    /**
     * Get attribute statistics by data type
     */
    @Query("SELECT pa.dataType, COUNT(pa) FROM ProductAttribute pa WHERE pa.isActive = true GROUP BY pa.dataType")
    List<Object[]> getAttributeStatisticsByDataType();
    
    /**
     * Get attribute statistics by required status
     */
    @Query("SELECT pa.isRequired, COUNT(pa) FROM ProductAttribute pa WHERE pa.isActive = true GROUP BY pa.isRequired")
    List<Object[]> getAttributeStatisticsByRequired();
}
