package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.Promotion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, UUID>, JpaSpecificationExecutor<Promotion> {
    
    /**
     * Find promotion by code
     */
    Optional<Promotion> findByPromotionCode(String promotionCode);
    
    /**
     * Check if promotion code exists, excluding specific promotion ID
     */
    boolean existsByPromotionCodeAndPromotionIdNot(String promotionCode, UUID promotionId);
    
    /**
     * Check if promotion code exists
     */
    boolean existsByPromotionCode(String promotionCode);
    
    /**
     * Find promotions by promotion type
     */
    List<Promotion> findByPromotionTypePromotionTypeId(UUID promotionTypeId);
    
    /**
     * Find active promotions (within date range and not deleted)
     */
    @Query("SELECT p FROM Promotion p WHERE p.isDeleted = false AND p.isActive = true " +
           "AND (p.startAt IS NULL OR p.startAt <= :now) AND (p.endAt IS NULL OR p.endAt >= :now)")
    List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);
    
    /**
     * Find auto-apply promotions
     */
    @Query("SELECT p FROM Promotion p WHERE p.isDeleted = false AND p.isActive = true " +
           "AND (p.startAt IS NULL OR p.startAt <= :now) AND (p.endAt IS NULL OR p.endAt >= :now)")
    List<Promotion> findAutoApplyPromotions(@Param("now") LocalDateTime now);
    
    /**
     * Find expired promotions
     */
    @Query("SELECT p FROM Promotion p WHERE p.isDeleted = false AND p.endAt < :now")
    List<Promotion> findExpiredPromotions(@Param("now") LocalDateTime now);
    
    /**
     * Find promotions starting soon
     */
    @Query("SELECT p FROM Promotion p WHERE p.isDeleted = false AND p.isActive = true " +
           "AND p.startAt > :now AND p.startAt <= :futureTime")
    List<Promotion> findPromotionsStartingSoon(@Param("now") LocalDateTime now, 
                                               @Param("futureTime") LocalDateTime futureTime);
    
    /**
     * Find promotions by priority
     */
    List<Promotion> findByPriorityOrderByPriorityDesc(Integer priority);
    
    /**
     * Find stackable promotions
     */
    List<Promotion> findByIsStackableTrue();
    
    /**
     * Search promotions by name (case-insensitive)
     */
    @Query("SELECT p FROM Promotion p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Promotion> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Search promotions by name with limit
     */
    @Query("SELECT p FROM Promotion p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "ORDER BY p.name")
    List<Promotion> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    /**
     * Find promotions for autocomplete suggestions
     */
    @Query("SELECT p FROM Promotion p WHERE LOWER(p.name) LIKE LOWER(CONCAT(:partial, '%')) " +
           "AND (:promotionTypeId IS NULL OR p.promotionType.promotionTypeId = :promotionTypeId) " +
           "ORDER BY p.name")
    List<Promotion> findPromotionSuggestions(@Param("partial") String partial,
                                             @Param("promotionTypeId") UUID promotionTypeId,
                                             Pageable pageable);
    
    /**
     * Find promotions with available usage (usage limit not exceeded)
     */
    @Query("SELECT p FROM Promotion p WHERE p.usageLimit IS NULL OR SIZE(p.usages) < p.usageLimit")
    List<Promotion> findPromotionsWithAvailableUsage();
    
    
    /**
     * Get promotion statistics
     */
    @Query("SELECT COUNT(p) FROM Promotion p")
    long getTotalPromotionsCount();
    
    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.isDeleted = false")
    long getActivePromotionsCount();
    
    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.isDeleted = false AND p.isActive = true")
    long getActiveAndNotDeletedPromotionsCount();
    
    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.isDeleted = false AND p.isActive = true")
    long getVisiblePromotionsCount();
    
    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.isDeleted = false AND p.isActive = true")
    long getAutoApplyPromotionsCount();
    
    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.isDeleted = false AND p.isStackable = true")
    long getStackablePromotionsCount();
    
    /**
     * Get promotions by date range
     */
    @Query("SELECT p FROM Promotion p WHERE p.startAt BETWEEN :startDate AND :endDate " +
           "OR p.endAt BETWEEN :startDate AND :endDate")
    List<Promotion> findPromotionsByDateRange(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get promotions ending soon
     */
    @Query("SELECT p FROM Promotion p WHERE p.isDeleted = false AND p.isActive = true " +
           "AND p.endAt > :now AND p.endAt <= :futureTime")
    List<Promotion> findPromotionsEndingSoon(@Param("now") LocalDateTime now,
                                             @Param("futureTime") LocalDateTime futureTime);
    
    /**
     * Get most used promotions
     */
    @Query("SELECT p FROM Promotion p WHERE p.isDeleted = false ORDER BY SIZE(p.usages) DESC")
    List<Promotion> findMostUsedPromotions(Pageable pageable);
    
    /**
     * Get least used promotions
     */
    @Query("SELECT p FROM Promotion p WHERE p.isDeleted = false ORDER BY SIZE(p.usages) ASC")
    List<Promotion> findLeastUsedPromotions(Pageable pageable);
    
    
    /**
     * Get promotion usage statistics
     */
    @Query("SELECT SUM(SIZE(p.usages)) FROM Promotion p WHERE p.isDeleted = false")
    Long getTotalUsageCount();
    
    @Query("SELECT AVG(SIZE(p.usages)) FROM Promotion p WHERE p.isDeleted = false")
    Double getAverageUsageCount();
    
    @Query("SELECT MAX(SIZE(p.usages)) FROM Promotion p WHERE p.isDeleted = false")
    Integer getMaxUsageCount();
    
    /**
     * Find promotions that can be applied to an order
     */
    @Query("SELECT p FROM Promotion p WHERE p.isDeleted = false AND p.isActive = true " +
           "AND (p.startAt IS NULL OR p.startAt <= :now) AND (p.endAt IS NULL OR p.endAt >= :now) " +
           "AND (p.usageLimit IS NULL OR SIZE(p.usages) < p.usageLimit)")
    List<Promotion> findApplicablePromotions(@Param("now") LocalDateTime now);
    
    /**
     * Find promotions by multiple criteria
     */
    @Query("SELECT p FROM Promotion p WHERE p.isDeleted = false AND p.isActive = true " +
           "AND (:promotionTypeId IS NULL OR p.promotionType.promotionTypeId = :promotionTypeId) " +
           "AND (:branchId IS NULL OR p.branch.branchId = :branchId) " +
           "AND (:isStackable IS NULL OR p.isStackable = :isStackable) " +
           "AND (:couponRedeemOnce IS NULL OR p.couponRedeemOnce = :couponRedeemOnce)")
    List<Promotion> findPromotionsByMultipleCriteria(@Param("promotionTypeId") UUID promotionTypeId,
                                                     @Param("branchId") UUID branchId,
                                                     @Param("isStackable") Boolean isStackable,
                                                     @Param("couponRedeemOnce") Boolean couponRedeemOnce);
    
    /**
     * Get promotion reference by ID (for lazy loading)
     */
    @NonNull
    Promotion getReferenceById(@NonNull UUID promotionId);
    
    /**
     * Find promotion by ID (optional)
     */
    @NonNull
    Optional<Promotion> findById(@NonNull UUID promotionId);
    
    /**
     * Get promotion by ID (required)
     */
    default @NonNull Promotion getById(@NonNull UUID promotionId) {
        return findById(promotionId)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Promotion not found with ID: " + promotionId));
    }
    
    /**
     * Save promotion
     */
    @NonNull
    <S extends Promotion> S save(@NonNull S promotion);
    
    /**
     * Delete promotion (soft delete)
     */
    @Modifying
    @Query("UPDATE Promotion p SET p.isDeleted = true WHERE p.promotionId = :promotionId")
    void softDeletePromotion(@Param("promotionId") UUID promotionId);
    
    /**
     * Restore promotion (undo soft delete)
     */
    @Modifying
    @Query("UPDATE Promotion p SET p.isDeleted = false WHERE p.promotionId = :promotionId")
    void restorePromotion(@Param("promotionId") UUID promotionId);
    
    /**
     * Update promotion status
     */
    @Modifying
    @Query("UPDATE Promotion p SET p.isActive = :isActive WHERE p.promotionId = :promotionId")
    void updatePromotionStatus(@Param("promotionId") UUID promotionId, @Param("isActive") Boolean isActive);
    
    /**
     * Update promotion visibility (using isActive instead of isVisible)
     */
    @Modifying
    @Query("UPDATE Promotion p SET p.isActive = :isActive WHERE p.promotionId = :promotionId")
    void updatePromotionVisibility(@Param("promotionId") UUID promotionId, @Param("isActive") Boolean isActive);
}
