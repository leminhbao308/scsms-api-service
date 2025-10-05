package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.ServicePackageType;
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
 * Repository for ServicePackageType entity
 */
@Repository
public interface ServicePackageTypeRepository extends JpaRepository<ServicePackageType, UUID> {
    
    /**
     * Find service package type by code
     */
    Optional<ServicePackageType> findByCode(String code);
    
    /**
     * Check if service package type exists by code
     */
    boolean existsByCode(String code);
    
    /**
     * Check if service package type exists by code excluding specific ID
     */
    boolean existsByCodeAndServicePackageTypeIdNot(String code, UUID servicePackageTypeId);
    
    /**
     * Find all active service package types
     */
    List<ServicePackageType> findByIsActiveTrueAndIsDeletedFalse();
    
    /**
     * Find all active service package types ordered by name
     */
    List<ServicePackageType> findByIsActiveTrueAndIsDeletedFalseOrderByNameAsc();
    
    /**
     * Find default service package type
     */
    Optional<ServicePackageType> findByIsDefaultTrueAndIsActiveTrueAndIsDeletedFalse();
    
    /**
     * Find service package types by applicable customer type
     */
    @Query("SELECT spt FROM ServicePackageType spt WHERE CAST(spt.applicableCustomerType AS string) = :customerType AND spt.isActive = true AND spt.isDeleted = false")
    List<ServicePackageType> findByApplicableCustomerTypeAndIsActiveTrueAndIsDeletedFalse(@Param("customerType") String customerType);
    
    /**
     * Find service package types by name containing (case insensitive)
     */
    @Query("SELECT spt FROM ServicePackageType spt WHERE LOWER(CAST(spt.name AS string)) LIKE LOWER(CAST(CONCAT('%', :name, '%') AS string)) AND spt.isDeleted = false")
    List<ServicePackageType> findByNameContainingIgnoreCaseAndIsDeletedFalse(@Param("name") String name);
    
    /**
     * Find service package types by code containing (case insensitive)
     */
    @Query("SELECT spt FROM ServicePackageType spt WHERE LOWER(CAST(spt.code AS string)) LIKE LOWER(CAST(CONCAT('%', :code, '%') AS string)) AND spt.isDeleted = false")
    List<ServicePackageType> findByCodeContainingIgnoreCaseAndIsDeletedFalse(@Param("code") String code);
    
    /**
     * Search service package types by keyword (name or code)
     */
    @Query("SELECT spt FROM ServicePackageType spt WHERE " +
           "(LOWER(CAST(spt.name AS string)) LIKE LOWER(CAST(CONCAT('%', :keyword, '%') AS string)) OR " +
           "LOWER(CAST(spt.code AS string)) LIKE LOWER(CAST(CONCAT('%', :keyword, '%') AS string))) AND " +
           "spt.isDeleted = false")
    List<ServicePackageType> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * Search service package types by keyword with pagination
     */
    @Query("SELECT spt FROM ServicePackageType spt WHERE " +
           "(LOWER(CAST(spt.name AS string)) LIKE LOWER(CAST(CONCAT('%', :keyword, '%') AS string)) OR " +
           "LOWER(CAST(spt.code AS string)) LIKE LOWER(CAST(CONCAT('%', :keyword, '%') AS string))) AND " +
           "spt.isDeleted = false")
    Page<ServicePackageType> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Find service package types with filters
     */
    @Query("SELECT spt FROM ServicePackageType spt WHERE " +
           "(:isActive IS NULL OR spt.isActive = :isActive) AND " +
           "(:isDefault IS NULL OR spt.isDefault = :isDefault) AND " +
           "(:customerType IS NULL OR CAST(spt.applicableCustomerType AS string) = :customerType) AND " +
           "(:keyword IS NULL OR " +
           "LOWER(CAST(spt.name AS string)) LIKE LOWER(CAST(CONCAT('%', :keyword, '%') AS string)) OR " +
           "LOWER(CAST(spt.code AS string)) LIKE LOWER(CAST(CONCAT('%', :keyword, '%') AS string))) AND " +
           "spt.isDeleted = false")
    Page<ServicePackageType> findByFilters(@Param("isActive") Boolean isActive,
                                          @Param("isDefault") Boolean isDefault,
                                          @Param("customerType") String customerType,
                                          @Param("keyword") String keyword,
                                          Pageable pageable);
    
    /**
     * Count active service package types
     */
    long countByIsActiveTrueAndIsDeletedFalse();
    
    /**
     * Count inactive service package types
     */
    long countByIsActiveFalseAndIsDeletedFalse();
    
    /**
     * Count default service package types
     */
    long countByIsDefaultTrueAndIsDeletedFalse();
    
    /**
     * Find service package types by default status
     */
    List<ServicePackageType> findByIsDefaultTrueAndIsDeletedFalse();
    
    /**
     * Find service package types by customer type
     */
    @Query("SELECT spt FROM ServicePackageType spt WHERE " +
           "(spt.applicableCustomerType IS NULL OR CAST(spt.applicableCustomerType AS string) = :customerType) AND " +
           "spt.isActive = true AND spt.isDeleted = false")
    List<ServicePackageType> findApplicableForCustomerType(@Param("customerType") String customerType);
    
    /**
     * Find service package types with price policy
     */
    List<ServicePackageType> findByPricePolicyIsNotNullAndIsActiveTrueAndIsDeletedFalse();
}
