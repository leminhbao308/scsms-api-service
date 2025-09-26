package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {
    
    // Find by unique fields
    Optional<Service> findByServiceUrl(String serviceUrl);
    
    // Existence checks
    boolean existsByServiceUrl(String serviceUrl);
    
    // Find by category
    List<Service> findByCategoryCategoryId(UUID categoryId);
    List<Service> findByCategoryCategoryIdAndIsActiveTrue(UUID categoryId);
    
    // Find by service type
    List<Service> findByServiceType(Service.ServiceType serviceType);
    List<Service> findByServiceTypeAndIsActiveTrue(Service.ServiceType serviceType);
    
    // Find by skill level
    List<Service> findByRequiredSkillLevel(Service.SkillLevel skillLevel);
    List<Service> findByRequiredSkillLevelAndIsActiveTrue(Service.SkillLevel skillLevel);
    
    // Find by complexity level
    List<Service> findByComplexityLevel(Service.ComplexityLevel complexityLevel);
    List<Service> findByComplexityLevelAndIsActiveTrue(Service.ComplexityLevel complexityLevel);
    
    // Find by duration range
    @Query("SELECT s FROM Service s WHERE s.standardDuration BETWEEN :minDuration AND :maxDuration AND s.isActive = true")
    List<Service> findByDurationRange(@Param("minDuration") Integer minDuration, @Param("maxDuration") Integer maxDuration);
    
    // Find by price range
    @Query("SELECT s FROM Service s WHERE s.basePrice BETWEEN :minPrice AND :maxPrice AND s.isActive = true")
    List<Service> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    // Find package services
    List<Service> findByIsPackageTrueAndIsActiveTrue();
    
    // Find non-package services
    List<Service> findByIsPackageFalseAndIsActiveTrue();
    
    // Find express services
    List<Service> findByIsExpressServiceTrueAndIsActiveTrue();
    
    // Find premium services
    List<Service> findByIsPremiumServiceTrueAndIsActiveTrue();
    
    // Find featured services
    List<Service> findByIsFeaturedTrueAndIsActiveTrue();
    
    // Find services requiring photos
    List<Service> findByPhotoRequiredTrueAndIsActiveTrue();
    
    // Find services requiring customer approval
    List<Service> findByCustomerApprovalRequiredTrueAndIsActiveTrue();
    
    // Search by name or description
    @Query("SELECT s FROM Service s WHERE " +
           "(LOWER(s.serviceName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.safetyNotes) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.qualityCriteria) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "s.isActive = true")
    List<Service> searchByKeyword(@Param("keyword") String keyword);
    
    // Find by tags (JSON search) - using JSON_CONTAINS for MySQL
    @Query(value = "SELECT * FROM services s WHERE JSON_CONTAINS(s.tags, :tag) AND s.is_active = true", nativeQuery = true)
    List<Service> findByTag(@Param("tag") String tag);
    
    // Find by vehicle types (JSON search) - using JSON_CONTAINS for MySQL
    @Query(value = "SELECT * FROM services s WHERE JSON_CONTAINS(s.vehicle_types, :vehicleType) AND s.is_active = true", nativeQuery = true)
    List<Service> findByVehicleType(@Param("vehicleType") String vehicleType);
    
    // Find by required tools (JSON search) - using JSON_CONTAINS for MySQL
    @Query(value = "SELECT * FROM services s WHERE JSON_CONTAINS(s.required_tools, :tool) AND s.is_active = true", nativeQuery = true)
    List<Service> findByRequiredTool(@Param("tool") String tool);
    
    // Find services by multiple criteria
    @Query("SELECT s FROM Service s WHERE " +
           "(:serviceType IS NULL OR s.serviceType = :serviceType) AND " +
           "(:skillLevel IS NULL OR s.requiredSkillLevel = :skillLevel) AND " +
           "(:complexityLevel IS NULL OR s.complexityLevel = :complexityLevel) AND " +
           "(:isExpress IS NULL OR s.isExpressService = :isExpress) AND " +
           "(:isPremium IS NULL OR s.isPremiumService = :isPremium) AND " +
           "s.isActive = true")
    List<Service> findByMultipleCriteria(
        @Param("serviceType") Service.ServiceType serviceType,
        @Param("skillLevel") Service.SkillLevel skillLevel,
        @Param("complexityLevel") Service.ComplexityLevel complexityLevel,
        @Param("isExpress") Boolean isExpress,
        @Param("isPremium") Boolean isPremium
    );
    
    // Count by category
    long countByCategoryCategoryId(UUID categoryId);
    long countByCategoryCategoryIdAndIsActiveTrue(UUID categoryId);
    
    // Count by service type
    long countByServiceType(Service.ServiceType serviceType);
    long countByServiceTypeAndIsActiveTrue(Service.ServiceType serviceType);
    
    // Count by skill level
    long countByRequiredSkillLevel(Service.SkillLevel skillLevel);
    long countByRequiredSkillLevelAndIsActiveTrue(Service.SkillLevel skillLevel);
    
    // Get average duration by service type
    @Query("SELECT AVG(s.standardDuration) FROM Service s WHERE s.serviceType = :serviceType AND s.isActive = true")
    Double getAverageDurationByServiceType(@Param("serviceType") Service.ServiceType serviceType);
    
    // Get average price by service type
    @Query("SELECT AVG(s.basePrice) FROM Service s WHERE s.serviceType = :serviceType AND s.isActive = true")
    BigDecimal getAveragePriceByServiceType(@Param("serviceType") Service.ServiceType serviceType);
}