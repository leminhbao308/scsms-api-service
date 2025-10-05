package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.ServiceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ServiceType entity
 */
@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceType, UUID> {
    
    /**
     * Find service type by code
     */
    Optional<ServiceType> findByCode(String code);
    
    /**
     * Check if service type exists by code
     */
    boolean existsByCode(String code);
    
    /**
     * Check if service type exists by code excluding specific ID
     */
    boolean existsByCodeAndServiceTypeIdNot(String code, UUID serviceTypeId);
    
    /**
     * Find all active service types
     */
    List<ServiceType> findByIsActiveTrueAndIsDeletedFalse();
    
    /**
     * Find all active service types ordered by name
     */
    List<ServiceType> findByIsActiveTrueAndIsDeletedFalseOrderByNameAsc();
    
    /**
     * Find service types by name containing (case insensitive)
     */
    @Query("SELECT st FROM ServiceType st WHERE LOWER(CAST(st.name AS string)) LIKE LOWER(CAST(CONCAT('%', :name, '%') AS string)) AND st.isDeleted = false")
    List<ServiceType> findByNameContainingIgnoreCaseAndIsDeletedFalse(@Param("name") String name);
    
    /**
     * Find service types by code containing (case insensitive)
     */
    @Query("SELECT st FROM ServiceType st WHERE LOWER(CAST(st.code AS string)) LIKE LOWER(CAST(CONCAT('%', :code, '%') AS string)) AND st.isDeleted = false")
    List<ServiceType> findByCodeContainingIgnoreCaseAndIsDeletedFalse(@Param("code") String code);
    
    /**
     * Search service types by keyword (name or code)
     */
    @Query("SELECT st FROM ServiceType st WHERE " +
           "(LOWER(CAST(st.name AS string)) LIKE LOWER(CAST(CONCAT('%', :keyword, '%') AS string)) OR " +
           "LOWER(CAST(st.code AS string)) LIKE LOWER(CAST(CONCAT('%', :keyword, '%') AS string))) AND " +
           "st.isDeleted = false")
    List<ServiceType> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * Search service types by keyword with pagination
     */
    @Query("SELECT st FROM ServiceType st WHERE " +
           "(LOWER(CAST(st.name AS string)) LIKE LOWER(CAST(CONCAT('%', :keyword, '%') AS string)) OR " +
           "LOWER(CAST(st.code AS string)) LIKE LOWER(CAST(CONCAT('%', :keyword, '%') AS string))) AND " +
           "st.isDeleted = false")
    Page<ServiceType> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Find service types with filters
     */
    @Query("SELECT st FROM ServiceType st WHERE " +
           "(:isActive IS NULL OR st.isActive = :isActive) AND " +
           "(:keyword IS NULL OR " +
           "LOWER(CAST(st.name AS string)) LIKE LOWER(CAST(CONCAT('%', :keyword, '%') AS string)) OR " +
           "LOWER(CAST(st.code AS string)) LIKE LOWER(CAST(CONCAT('%', :keyword, '%') AS string))) AND " +
           "st.isDeleted = false")
    Page<ServiceType> findByFilters(@Param("isActive") Boolean isActive, 
                                   @Param("keyword") String keyword, 
                                   Pageable pageable);
    
    /**
     * Count active service types
     */
    long countByIsActiveTrueAndIsDeletedFalse();
    
    /**
     * Count inactive service types
     */
    long countByIsActiveFalseAndIsDeletedFalse();
    
    /**
     * Find service types by default duration range
     */
    List<ServiceType> findByDefaultDurationBetweenAndIsDeletedFalse(Integer minDuration, Integer maxDuration);
    
    /**
     * Find service types with no default duration
     */
    List<ServiceType> findByDefaultDurationIsNullAndIsDeletedFalse();
}
