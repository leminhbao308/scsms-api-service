package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.ServicePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    List<ServicePackage> findByPackageType(ServicePackage.PackageType packageType);
    List<ServicePackage> findByPackageTypeAndIsActiveTrue(ServicePackage.PackageType packageType);
    
    // Find by price range
    @Query("SELECT sp FROM ServicePackage sp WHERE sp.packagePrice BETWEEN :minPrice AND :maxPrice AND sp.isActive = true")
    List<ServicePackage> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    // Find by duration range
    @Query("SELECT sp FROM ServicePackage sp WHERE sp.totalDuration BETWEEN :minDuration AND :maxDuration AND sp.isActive = true")
    List<ServicePackage> findByDurationRange(@Param("minDuration") Integer minDuration, @Param("maxDuration") Integer maxDuration);
    
    // Find by discount percentage
    @Query("SELECT sp FROM ServicePackage sp WHERE sp.discountPercentage >= :minDiscount AND sp.isActive = true")
    List<ServicePackage> findByMinDiscount(@Param("minDiscount") BigDecimal minDiscount);
    
    // Find popular packages
    List<ServicePackage> findByIsPopularTrueAndIsActiveTrue();
    
    // Find recommended packages
    List<ServicePackage> findByIsRecommendedTrueAndIsActiveTrue();
    
    // Find limited time packages
    List<ServicePackage> findByIsLimitedTimeTrueAndIsActiveTrue();
    
    // Find packages by validity period
    @Query("SELECT sp FROM ServicePackage sp WHERE sp.validityPeriodDays <= :maxValidityDays AND sp.isActive = true")
    List<ServicePackage> findByMaxValidityPeriod(@Param("maxValidityDays") Integer maxValidityDays);
    
    // Find packages by usage count
    @Query("SELECT sp FROM ServicePackage sp WHERE sp.maxUsageCount <= :maxUsage AND sp.isActive = true")
    List<ServicePackage> findByMaxUsageCount(@Param("maxUsage") Integer maxUsage);
    
    // Find packages by date range
    @Query("SELECT sp FROM ServicePackage sp WHERE " +
           "(:startDate IS NULL OR sp.startDate >= :startDate) AND " +
           "(:endDate IS NULL OR sp.endDate <= :endDate) AND " +
           "sp.isActive = true")
    List<ServicePackage> findByDateRange(
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );
    
    // Find currently active packages (within date range)
    @Query("SELECT sp FROM ServicePackage sp WHERE " +
           "sp.isLimitedTime = true AND " +
           "sp.startDate <= CURRENT_DATE AND " +
           "(sp.endDate IS NULL OR sp.endDate >= CURRENT_DATE) AND " +
           "sp.isActive = true")
    List<ServicePackage> findCurrentlyActivePackages();
    
    // Find expired packages
    @Query("SELECT sp FROM ServicePackage sp WHERE " +
           "sp.isLimitedTime = true AND " +
           "sp.endDate < CURRENT_DATE AND " +
           "sp.isActive = true")
    List<ServicePackage> findExpiredPackages();
    
    // Find upcoming packages
    @Query("SELECT sp FROM ServicePackage sp WHERE " +
           "sp.isLimitedTime = true AND " +
           "sp.startDate > CURRENT_DATE AND " +
           "sp.isActive = true")
    List<ServicePackage> findUpcomingPackages();
    
    // Search by name or description
    @Query("SELECT sp FROM ServicePackage sp WHERE " +
           "(LOWER(sp.packageName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(sp.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(sp.termsAndConditions) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "sp.isActive = true")
    List<ServicePackage> searchByKeyword(@Param("keyword") String keyword);
    
    // Find by tags (JSON search) - using JSON_CONTAINS for MySQL
    @Query(value = "SELECT * FROM service_packages sp WHERE JSON_CONTAINS(sp.tags, :tag) AND sp.is_active = true", nativeQuery = true)
    List<ServicePackage> findByTag(@Param("tag") String tag);
    
    // Find by target vehicle types (JSON search) - using JSON_CONTAINS for MySQL
    @Query(value = "SELECT * FROM service_packages sp WHERE JSON_CONTAINS(sp.target_vehicle_types, :vehicleType) AND sp.is_active = true", nativeQuery = true)
    List<ServicePackage> findByTargetVehicleType(@Param("vehicleType") String vehicleType);
    
    // Find packages by multiple criteria
    @Query("SELECT sp FROM ServicePackage sp WHERE " +
           "(:packageType IS NULL OR sp.packageType = :packageType) AND " +
           "(:isPopular IS NULL OR sp.isPopular = :isPopular) AND " +
           "(:isRecommended IS NULL OR sp.isRecommended = :isRecommended) AND " +
           "(:isLimitedTime IS NULL OR sp.isLimitedTime = :isLimitedTime) AND " +
           "sp.isActive = true")
    List<ServicePackage> findByMultipleCriteria(
        @Param("packageType") ServicePackage.PackageType packageType,
        @Param("isPopular") Boolean isPopular,
        @Param("isRecommended") Boolean isRecommended,
        @Param("isLimitedTime") Boolean isLimitedTime
    );
    
    // Find packages with best savings
    @Query("SELECT sp FROM ServicePackage sp WHERE sp.savingsAmount > 0 AND sp.isActive = true ORDER BY sp.savingsAmount DESC")
    List<ServicePackage> findPackagesWithBestSavings();
    
    // Find packages with highest discount
    @Query("SELECT sp FROM ServicePackage sp WHERE sp.discountPercentage > 0 AND sp.isActive = true ORDER BY sp.discountPercentage DESC")
    List<ServicePackage> findPackagesWithHighestDiscount();
    
    // Count by category
    long countByCategoryCategoryId(UUID categoryId);
    long countByCategoryCategoryIdAndIsActiveTrue(UUID categoryId);
    
    // Count by package type
    long countByPackageType(ServicePackage.PackageType packageType);
    long countByPackageTypeAndIsActiveTrue(ServicePackage.PackageType packageType);
    
    // Get average price by package type
    @Query("SELECT AVG(sp.packagePrice) FROM ServicePackage sp WHERE sp.packageType = :packageType AND sp.isActive = true")
    BigDecimal getAveragePriceByPackageType(@Param("packageType") ServicePackage.PackageType packageType);
    
    // Get average duration by package type
    @Query("SELECT AVG(sp.totalDuration) FROM ServicePackage sp WHERE sp.packageType = :packageType AND sp.isActive = true")
    Double getAverageDurationByPackageType(@Param("packageType") ServicePackage.PackageType packageType);
    
    // Get average discount by package type
    @Query("SELECT AVG(sp.discountPercentage) FROM ServicePackage sp WHERE sp.packageType = :packageType AND sp.isActive = true")
    BigDecimal getAverageDiscountByPackageType(@Param("packageType") ServicePackage.PackageType packageType);
}