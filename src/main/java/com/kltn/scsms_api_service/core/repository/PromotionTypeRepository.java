package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.PromotionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromotionTypeRepository extends JpaRepository<PromotionType, UUID> {
    
    /**
     * Find promotion type by type code
     */
    Optional<PromotionType> findByTypeCode(String typeCode);
    
    /**
     * Check if promotion type exists by type code
     */
    boolean existsByTypeCode(String typeCode);
    
    /**
     * Find promotion type by type code excluding specific ID (for update validation)
     */
    @Query("SELECT pt FROM PromotionType pt WHERE pt.typeCode = :typeCode AND pt.promotionTypeId != :excludeId")
    Optional<PromotionType> findByTypeCodeExcludingId(@Param("typeCode") String typeCode, @Param("excludeId") UUID excludeId);
    
    /**
     * Check if promotion type exists by type code excluding specific ID
     */
    @Query("SELECT COUNT(pt) > 0 FROM PromotionType pt WHERE pt.typeCode = :typeCode AND pt.promotionTypeId != :excludeId")
    boolean existsByTypeCodeExcludingId(@Param("typeCode") String typeCode, @Param("excludeId") UUID excludeId);
    
    /**
     * Find all active promotion types
     */
    @Query("SELECT pt FROM PromotionType pt WHERE pt.isActive = true AND pt.isDeleted = false ORDER BY pt.typeName ASC")
    List<PromotionType> findAllActive();
    
    /**
     * Find promotion types by name containing keyword (case insensitive)
     */
    @Query("SELECT pt FROM PromotionType pt WHERE LOWER(pt.typeName) LIKE LOWER(CONCAT('%', :keyword, '%')) AND pt.isDeleted = false ORDER BY pt.typeName ASC")
    List<PromotionType> findByTypeNameContainingIgnoreCase(@Param("keyword") String keyword);
    
    /**
     * Find promotion types by description containing keyword (case insensitive)
     */
    @Query("SELECT pt FROM PromotionType pt WHERE LOWER(pt.description) LIKE LOWER(CONCAT('%', :keyword, '%')) AND pt.isDeleted = false ORDER BY pt.typeName ASC")
    List<PromotionType> findByDescriptionContainingIgnoreCase(@Param("keyword") String keyword);
    
    /**
     * Search promotion types by keyword in name or description
     */
    @Query("SELECT pt FROM PromotionType pt WHERE " +
           "(LOWER(pt.typeName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(pt.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND pt.isDeleted = false ORDER BY pt.typeName ASC")
    List<PromotionType> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * Count active promotion types
     */
    @Query("SELECT COUNT(pt) FROM PromotionType pt WHERE pt.isActive = true AND pt.isDeleted = false")
    long countActive();
    
    /**
     * Count all promotion types (including inactive)
     */
    @Query("SELECT COUNT(pt) FROM PromotionType pt WHERE pt.isDeleted = false")
    long countAll();
}
