package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.ServicePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServicePackageRepository extends JpaRepository<ServicePackage, UUID> {
    
    // Find by unique fields
    Optional<ServicePackage> findByPackageUrl(String packageUrl);
    
    // Existence checks
    boolean existsByPackageUrl(String packageUrl);
    
    // Find by category
    List<ServicePackage> findByCategoryCategoryId(UUID categoryId);
    List<ServicePackage> findByCategoryCategoryIdAndIsActiveTrue(UUID categoryId);
    
    // Find by package type
    List<ServicePackage> findByServicePackageTypeId(UUID servicePackageTypeId);
    List<ServicePackage> findByServicePackageTypeIdAndIsActiveTrue(UUID servicePackageTypeId);
    
    // Find by price range
    @Query("SELECT sp FROM ServicePackage sp WHERE sp.packagePrice BETWEEN :minPrice AND :maxPrice AND sp.isActive = true")
    List<ServicePackage> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    // Find by duration range
    @Query("SELECT sp FROM ServicePackage sp WHERE sp.totalDuration BETWEEN :minDuration AND :maxDuration AND sp.isActive = true")
    List<ServicePackage> findByDurationRange(@Param("minDuration") Integer minDuration, @Param("maxDuration") Integer maxDuration);
    
    // Search by name or description
    @Query("SELECT sp FROM ServicePackage sp WHERE " +
           "(LOWER(sp.packageName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(sp.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "sp.isActive = true")
    List<ServicePackage> searchByKeyword(@Param("keyword") String keyword);
    
    // Count by category
    long countByCategoryCategoryId(UUID categoryId);
    long countByCategoryCategoryIdAndIsActiveTrue(UUID categoryId);
    
    // Count by package type
    long countByServicePackageTypeId(UUID servicePackageTypeId);
    long countByServicePackageTypeIdAndIsActiveTrue(UUID servicePackageTypeId);
    
    // Get average price by package type
    @Query("SELECT AVG(sp.packagePrice) FROM ServicePackage sp WHERE sp.servicePackageTypeId = :servicePackageTypeId AND sp.isActive = true")
    BigDecimal getAveragePriceByPackageType(@Param("servicePackageTypeId") UUID servicePackageTypeId);
    
    // Get average duration by package type
    @Query("SELECT AVG(sp.totalDuration) FROM ServicePackage sp WHERE sp.servicePackageTypeId = :servicePackageTypeId AND sp.isActive = true")
    Double getAverageDurationByPackageType(@Param("servicePackageTypeId") UUID servicePackageTypeId);
}