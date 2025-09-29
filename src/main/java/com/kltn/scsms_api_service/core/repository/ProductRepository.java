package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    // Find featured products
    List<Product> findByIsFeaturedTrueAndIsActiveTrue();
    
    // Find low stock products (current stock <= min stock level)
    @Query("SELECT p FROM Product p WHERE p.minStockLevel > 0 AND p.isActive = true")
    List<Product> findLowStockProducts();
    
    // Find products with warranty
    @Query("SELECT p FROM Product p WHERE p.warrantyPeriodMonths > 0 AND p.isActive = true")
    List<Product> findProductsWithWarranty();
    
    // Find by weight range
    @Query("SELECT p FROM Product p WHERE p.weight BETWEEN :minWeight AND :maxWeight AND p.isActive = true")
    List<Product> findByWeightRange(@Param("minWeight") BigDecimal minWeight, @Param("maxWeight") BigDecimal maxWeight);
    
    // Search by name, description, brand, or SKU
    @Query("SELECT p FROM Product p WHERE " +
           "(LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "p.isActive = true")
    List<Product> searchByKeyword(@Param("keyword") String keyword);
    
    // Find by specifications (JSON search) - for products like LED lights
    @Query(value = "SELECT * FROM dev.products p WHERE " +
           "JSON_EXTRACT(p.specifications, '$.brightness') LIKE CONCAT('%', :brightness, '%') " +
           "AND p.is_active = true", nativeQuery = true)
    List<Product> findByBrightness(@Param("brightness") String brightness);
    
    // Count by category
    long countByCategoryCategoryId(UUID categoryId);
    long countByCategoryCategoryIdAndIsActiveTrue(UUID categoryId);
    
    // Count by supplier
    long countBySupplierId(UUID supplierId);
    long countBySupplierIdAndIsActiveTrue(UUID supplierId);
    
    // Count products with warranty
    @Query("SELECT COUNT(p) FROM Product p WHERE p.warrantyPeriodMonths > 0 AND p.isActive = true")
    long countProductsWithWarranty();
}