package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.ProductAttributeValue;
import com.kltn.scsms_api_service.core.entity.ProductAttributeValueId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, ProductAttributeValueId>, JpaSpecificationExecutor<ProductAttributeValue> {
    
    /**
     * Find all attribute values for a specific product
     */
    @Query("SELECT pav FROM ProductAttributeValue pav " +
           "JOIN FETCH pav.productAttribute pa " +
           "WHERE pav.product.productId = :productId AND pav.isDeleted = false")
    List<ProductAttributeValue> findByProductIdAndIsDeletedFalse(@Param("productId") UUID productId);
    
    /**
     * Find all products that have a specific attribute value
     */
    @Query("SELECT pav FROM ProductAttributeValue pav " +
           "JOIN FETCH pav.product p " +
           "WHERE pav.productAttribute.attributeId = :attributeId AND pav.isDeleted = false")
    List<ProductAttributeValue> findByAttributeIdAndIsDeletedFalse(@Param("attributeId") UUID attributeId);
    
    /**
     * Find attribute value for a specific product and attribute
     */
    @Query("SELECT pav FROM ProductAttributeValue pav " +
           "JOIN FETCH pav.productAttribute pa " +
           "WHERE pav.product.productId = :productId AND pav.productAttribute.attributeId = :attributeId AND pav.isDeleted = false")
    ProductAttributeValue findByProductIdAndAttributeIdAndIsDeletedFalse(
            @Param("productId") UUID productId,
            @Param("attributeId") UUID attributeId);
    
    /**
     * Find products by attribute value (text search)
     */
    @Query("SELECT pav FROM ProductAttributeValue pav " +
           "JOIN FETCH pav.product p " +
           "WHERE pav.productAttribute.attributeId = :attributeId AND LOWER(pav.valueText) LIKE LOWER(CONCAT('%', :value, '%')) AND pav.isDeleted = false")
    List<ProductAttributeValue> findByAttributeIdAndValueTextContainingIgnoreCaseAndIsDeletedFalse(
            @Param("attributeId") UUID attributeId, 
            @Param("value") String value);
    
    /**
     * Find products by attribute value (numeric range)
     */
    @Query("SELECT pav FROM ProductAttributeValue pav " +
           "JOIN FETCH pav.product p " +
           "WHERE pav.productAttribute.attributeId = :attributeId AND pav.valueNumber BETWEEN :minValue AND :maxValue AND pav.isDeleted = false")
    List<ProductAttributeValue> findByAttributeIdAndValueNumberBetweenAndIsDeletedFalse(
            @Param("attributeId") UUID attributeId, 
            @Param("minValue") java.math.BigDecimal minValue, 
            @Param("maxValue") java.math.BigDecimal maxValue);
    
    /**
     * Delete all attribute values for a product (soft delete)
     */
    @Query("UPDATE ProductAttributeValue pav SET pav.isDeleted = true, pav.isActive = false WHERE pav.product.productId = :productId")
    void softDeleteByProductId(@Param("productId") UUID productId);
    
    /**
     * Delete all attribute values for an attribute (soft delete)
     */
    @Query("UPDATE ProductAttributeValue pav SET pav.isDeleted = true, pav.isActive = false WHERE pav.productAttribute.attributeId = :attributeId")
    void softDeleteByAttributeId(@Param("attributeId") UUID attributeId);
    
    /**
     * Count attribute values for a product
     */
    @Query("SELECT COUNT(pav) FROM ProductAttributeValue pav WHERE pav.product.productId = :productId AND pav.isDeleted = false")
    long countByProductIdAndIsDeletedFalse(@Param("productId") UUID productId);
    
    /**
     * Count products that have a specific attribute
     */
    @Query("SELECT COUNT(pav) FROM ProductAttributeValue pav WHERE pav.productAttribute.attributeId = :attributeId AND pav.isDeleted = false")
    long countByAttributeIdAndIsDeletedFalse(@Param("attributeId") UUID attributeId);
}
