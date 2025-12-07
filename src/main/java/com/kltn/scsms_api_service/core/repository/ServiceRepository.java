package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID>, JpaSpecificationExecutor<Service> {
    
    // Find by unique fields
    Optional<Service> findByServiceUrl(String serviceUrl);
    
    // Existence checks
    boolean existsByServiceUrl(String serviceUrl);
    
    // Find by category
    List<Service> findByCategoryCategoryId(UUID categoryId);
    List<Service> findByCategoryCategoryIdAndIsActiveTrue(UUID categoryId);
    
    // Find by service type
    List<Service> findByServiceTypeId(UUID serviceTypeId);
    List<Service> findByServiceTypeIdAndIsActiveTrue(UUID serviceTypeId);
    
    // Find by skill level
    List<Service> findByRequiredSkillLevel(Service.SkillLevel skillLevel);
    List<Service> findByRequiredSkillLevelAndIsActiveTrue(Service.SkillLevel skillLevel);
    
    
    // Find by duration range
    @Query("SELECT s FROM Service s WHERE s.estimatedDuration BETWEEN :minDuration AND :maxDuration AND s.isActive = true")
    List<Service> findByDurationRange(@Param("minDuration") Integer minDuration, @Param("maxDuration") Integer maxDuration);
    
    
    
    
    // Find featured services
    List<Service> findByIsFeaturedTrueAndIsActiveTrue();
    
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
    long countByServiceTypeId(UUID serviceTypeId);
    long countByServiceTypeIdAndIsActiveTrue(UUID serviceTypeId);
    
    // Count by skill level
    long countByRequiredSkillLevel(Service.SkillLevel skillLevel);
    long countByRequiredSkillLevelAndIsActiveTrue(Service.SkillLevel skillLevel);
    
    // Get average duration by service type
    @Query("SELECT AVG(s.estimatedDuration) FROM Service s WHERE s.serviceTypeId = :serviceTypeId AND s.isActive = true")
    Double getAverageDurationByServiceType(@Param("serviceTypeId") UUID serviceTypeId);
    
}