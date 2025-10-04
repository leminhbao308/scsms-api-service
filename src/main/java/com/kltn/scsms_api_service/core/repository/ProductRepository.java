package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    
    // Find by unique fields
    Optional<Product> findByProductUrlAndIsDeletedFalse(String productUrl);
    Optional<Product> findBySkuAndIsDeletedFalse(String sku);
    Optional<Product> findByBarcodeAndIsDeletedFalse(String barcode);
    
    // Existence checks
    boolean existsByProductUrlAndIsDeletedFalse(String productUrl);
    boolean existsBySkuAndIsDeletedFalse(String sku);
    boolean existsByBarcodeAndIsDeletedFalse(String barcode);
    
    // Find by product type
    @Query("SELECT p FROM Product p JOIN FETCH p.productType pt WHERE pt.productTypeId = :productTypeId AND p.isDeleted = false")
    List<Product> findByProductTypeProductTypeIdAndIsDeletedFalse(@Param("productTypeId") UUID productTypeId);
    
    @Query("SELECT p FROM Product p JOIN FETCH p.productType pt WHERE pt.productTypeId = :productTypeId AND p.isActive = true AND p.isDeleted = false")
    List<Product> findByProductTypeProductTypeIdAndIsActiveTrueAndIsDeletedFalse(@Param("productTypeId") UUID productTypeId);
    
    // Find by supplier
    List<Product> findBySupplierIdAndIsDeletedFalse(UUID supplierId);
    List<Product> findBySupplierIdAndIsActiveTrueAndIsDeletedFalse(UUID supplierId);
    
    // Find by brand
    List<Product> findByBrandAndIsDeletedFalse(String brand);
    List<Product> findByBrandAndIsActiveTrueAndIsDeletedFalse(String brand);
    
    // Find featured products
    List<Product> findByIsFeaturedTrueAndIsActiveTrueAndIsDeletedFalse();
    
    
    // Search by name, description, brand, or SKU
    @Query("SELECT p FROM Product p WHERE " +
           "(LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "p.isActive = true AND p.isDeleted = false")
    List<Product> searchByKeyword(@Param("keyword") String keyword);
    
    // Count by product type
    long countByProductTypeProductTypeIdAndIsDeletedFalse(UUID productTypeId);
    long countByProductTypeProductTypeIdAndIsActiveTrueAndIsDeletedFalse(UUID productTypeId);
    
    // Count by supplier
    long countBySupplierIdAndIsDeletedFalse(UUID supplierId);
    long countBySupplierIdAndIsActiveTrueAndIsDeletedFalse(UUID supplierId);
    
    
    // Count all active products
    long countByIsActiveTrueAndIsDeletedFalse();
    
    // Count all products
    long countByIsDeletedFalse();
}