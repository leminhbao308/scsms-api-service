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
    
    
    // Find featured services
    List<Service> findByIsFeaturedTrueAndIsActiveTrue();
    
    // Find services requiring photos
    List<Service> findByPhotoRequiredTrueAndIsActiveTrue();
    
    
    // Search by name or description
    @Query("SELECT s FROM Service s WHERE " +
           "(LOWER(s.serviceName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "s.isActive = true")
    List<Service> searchByKeyword(@Param("keyword") String keyword);
    
    
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