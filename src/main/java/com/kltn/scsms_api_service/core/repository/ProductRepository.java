package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    
    // Find by unique fields
    Optional<Product> findByProductUrl(String productUrl);
    Optional<Product> findBySku(String sku);
    Optional<Product> findByBarcode(String barcode);
    
    // Existence checks
    boolean existsByProductUrl(String productUrl);
    boolean existsBySku(String sku);
    boolean existsByBarcode(String barcode);
    
    // Find by category
    List<Product> findByCategoryCategoryId(UUID categoryId);
    List<Product> findByCategoryCategoryIdAndIsActiveTrue(UUID categoryId);
    
    // Find by supplier
    List<Product> findBySupplierId(UUID supplierId);
    List<Product> findBySupplierIdAndIsActiveTrue(UUID supplierId);
    
    // Find by brand
    List<Product> findByBrand(String brand);
    List<Product> findByBrandAndIsActiveTrue(String brand);
    
    // Find by price range
    @Query("SELECT p FROM Product p WHERE p.sellingPrice BETWEEN :minPrice AND :maxPrice AND p.isActive = true")
    List<Product> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
    
    // Find featured products
    List<Product> findByIsFeaturedTrueAndIsActiveTrue();
    
    // Find by stock level
    @Query("SELECT p FROM Product p WHERE p.minStockLevel >= p.reorderPoint AND p.isActive = true")
    List<Product> findLowStockProducts();
    
    // Search by name or description
    @Query("SELECT p FROM Product p WHERE " +
           "(LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "p.isActive = true")
    List<Product> searchByKeyword(@Param("keyword") String keyword);
    
    // Find by tags (JSON search) - using JSON_CONTAINS for MySQL or similar for other databases
    @Query(value = "SELECT * FROM products p WHERE JSON_CONTAINS(p.tags, :tag) AND p.is_active = true", nativeQuery = true)
    List<Product> findByTag(@Param("tag") String tag);
    
    // Find trackable products
    List<Product> findByIsTrackableTrueAndIsActiveTrue();
    
    // Find consumable products
    List<Product> findByIsConsumableTrueAndIsActiveTrue();
    
    // Find by weight range
    @Query("SELECT p FROM Product p WHERE p.weight BETWEEN :minWeight AND :maxWeight AND p.isActive = true")
    List<Product> findByWeightRange(@Param("minWeight") Double minWeight, @Param("maxWeight") Double maxWeight);
    
    // Find products with warranty
    @Query("SELECT p FROM Product p WHERE p.warrantyPeriodMonths > 0 AND p.isActive = true")
    List<Product> findProductsWithWarranty();
    
    // Count by category
    long countByCategoryCategoryId(UUID categoryId);
    long countByCategoryCategoryIdAndIsActiveTrue(UUID categoryId);
    
    // Count by supplier
    long countBySupplierId(UUID supplierId);
    long countBySupplierIdAndIsActiveTrue(UUID supplierId);
}