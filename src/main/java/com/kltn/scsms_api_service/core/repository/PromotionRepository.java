package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.Promotion;
import org.springframework.data.domain.Page;
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
     * Find promotions by category
     */
    List<Promotion> findByCategoryCategoryId(UUID categoryId);
    
    /**
     * Find promotions by category with pagination
     */
    Page<Promotion> findByCategoryCategoryId(UUID categoryId, Pageable pageable);
    
    /**
     * Find promotions by type
     */
    List<Promotion> findByPromotionType(String promotionType);
    
    /**
     * Find promotions by discount type
     */
    List<Promotion> findByDiscountType(Promotion.DiscountType discountType);
    
    /**
     * Find active promotions (within date range and not deleted)
     */
    @Query("SELECT p FROM Promotion p WHERE p.isDeleted = false AND p.isActive = true " +
           "AND p.startDate <= :now AND p.endDate >= :now")
    List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);
    
    /**
     * Find visible promotions
     */
    @Query("SELECT p FROM Promotion p WHERE p.isDeleted = false AND p.isActive = true " +
           "AND p.isVisible = true AND p.startDate <= :now AND p.endDate >= :now")
    List<Promotion> findVisiblePromotions(@Param("now") LocalDateTime now);
    
    /**
     * Find auto-apply promotions
     */
    @Query("SELECT p FROM Promotion p WHERE p.isDeleted = false AND p.isActive = true " +
           "AND p.autoApply = true AND p.startDate <= :now AND p.endDate >= :now")
    List<Promotion> findAutoApplyPromotions(@Param("now") LocalDateTime now);
    
    /**
     * Find expired promotions
     */
    @Query("SELECT p FROM Promotion p WHERE p.isDeleted = false AND p.endDate < :now")
    List<Promotion> findExpiredPromotions(@Param("now") LocalDateTime now);
    
    /**
     * Find promotions starting soon
     */
    @Query("SELECT p FROM Promotion p WHERE p.isDeleted = false AND p.isActive = true " +
           "AND p.startDate > :now AND p.startDate <= :futureTime")
    List<Promotion> findPromotionsStartingSoon(@Param("now") LocalDateTime now, 
                                               @Param("futureTime") LocalDateTime futureTime);
    
    /**
     * Find promotions by priority
     */
    List<Promotion> findByPriorityOrderByPriorityDesc(Integer priority);
    
    /**
     * Find stackable promotions
     */
    List<Promotion> findByStackableTrue();
    
    /**
     * Find promotions that don't require coupon code
     */
    List<Promotion> findByRequireCouponCodeFalse();
    
    /**
     * Search promotions by name (case-insensitive)
     */
    @Query("SELECT p FROM Promotion p WHERE LOWER(p.promotionName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Promotion> findByPromotionNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Search promotions by name with limit
     */
    @Query("SELECT p FROM Promotion p WHERE LOWER(p.promotionName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "ORDER BY p.promotionName")
    List<Promotion> findByPromotionNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    /**
     * Find promotions for autocomplete suggestions
     */
    @Query("SELECT p FROM Promotion p WHERE LOWER(p.promotionName) LIKE LOWER(CONCAT(:partial, '%')) " +
           "AND (:categoryId IS NULL OR p.category.categoryId = :categoryId) " +
           "ORDER BY p.promotionName")
    List<Promotion> findPromotionSuggestions(@Param("partial") String partial,
                                             @Param("categoryId") UUID categoryId,
                                             Pageable pageable);
    
    /**
     * Find promotions by usage limit status
     */
    @Query("SELECT p FROM Promotion p WHERE p.usageLimit IS NOT NULL AND p.usedCount >= p.usageLimit")
    List<Promotion> findPromotionsWithExceededUsageLimit();
    
    /**
     * Find promotions with available usage
     */
    @Query("SELECT p FROM Promotion p WHERE p.usageLimit IS NULL OR p.usedCount < p.usageLimit")
    List<Promotion> findPromotionsWithAvailableUsage();
    
    /**
     * Find promotions by minimum order amount range
     */
    @Query("SELECT p FROM Promotion p WHERE p.minOrderAmount BETWEEN :minAmount AND :maxAmount")
    List<Promotion> findByMinOrderAmountBetween(@Param("minAmount") Double minAmount, 
                                                @Param("maxAmount") Double maxAmount);
    
    /**
     * Find promotions by discount value range
     */
    @Query("SELECT p FROM Promotion p WHERE p.discountValue BETWEEN :minValue AND :maxValue")
    List<Promotion> findByDiscountValueBetween(@Param("minValue") Double minValue, 
                                               @Param("maxValue") Double maxValue);
    
    /**
     * Get promotion statistics
     */
    @Query("SELECT COUNT(p) FROM Promotion p")
    long getTotalPromotionsCount();
    
    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.isDeleted = false")
    long getActivePromotionsCount();
    
    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.isDeleted = false AND p.isActive = true")
    long getActiveAndNotDeletedPromotionsCount();
    
    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.isDeleted = false AND p.isVisible = true")
    long getVisiblePromotionsCount();
    
    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.isDeleted = false AND p.autoApply = true")
    long getAutoApplyPromotionsCount();
    
    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.isDeleted = false AND p.stackable = true")
    long getStackablePromotionsCount();
    
    /**
     * Get promotions by date range
     */
    @Query("SELECT p FROM Promotion p WHERE p.startDate BETWEEN :startDate AND :endDate " +
           "OR p.endDate BETWEEN :startDate AND :endDate")
    List<Promotion> findPromotionsByDateRange(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get promotions ending soon
     */
    @Query("SELECT p FROM Promotion p WHERE p.isDeleted = false AND p.isActive = true " +
           "AND p.endDate > :now AND p.endDate <= :futureTime")
    List<Promotion> findPromotionsEndingSoon(@Param("now") LocalDateTime now,
                                             @Param("futureTime") LocalDateTime futureTime);
    
    /**
     * Get most used promotions
     */
    @Query("SELECT p FROM Promotion p WHERE p.isDeleted = false ORDER BY p.usedCount DESC")
    List<Promotion> findMostUsedPromotions(Pageable pageable);
    
    /**
     * Get least used promotions
     */
    @Query("SELECT p FROM Promotion p WHERE p.isDeleted = false ORDER BY p.usedCount ASC")
    List<Promotion> findLeastUsedPromotions(Pageable pageable);
    
    /**
     * Get promotions by customer rank
     */
    @Query(value = "SELECT * FROM dev.promotions p WHERE p.is_deleted = false AND p.is_active = true " +
           "AND (p.target_customer_ranks IS NULL OR p.target_customer_ranks::text LIKE CONCAT('%', :rank, '%'))", 
           nativeQuery = true)
    List<Promotion> findPromotionsByCustomerRank(@Param("rank") String rank);
    
    /**
     * Get promotions by vehicle type
     */
    @Query(value = "SELECT * FROM dev.promotions p WHERE p.is_deleted = false AND p.is_active = true " +
           "AND (p.target_vehicle_types IS NULL OR p.target_vehicle_types::text LIKE CONCAT('%', :vehicleType, '%'))", 
           nativeQuery = true)
    List<Promotion> findPromotionsByVehicleType(@Param("vehicleType") String vehicleType);
    
    /**
     * Get promotions by branch
     */
    @Query(value = "SELECT * FROM dev.promotions p WHERE p.is_deleted = false AND p.is_active = true " +
           "AND (p.target_branches IS NULL OR p.target_branches::text LIKE CONCAT('%', :branchId, '%'))", 
           nativeQuery = true)
    List<Promotion> findPromotionsByBranch(@Param("branchId") String branchId);
    
    /**
     * Get promotions by service
     */
    @Query(value = "SELECT * FROM dev.promotions p WHERE p.is_deleted = false AND p.is_active = true " +
           "AND (p.target_services IS NULL OR p.target_services::text LIKE CONCAT('%', :serviceId, '%'))", 
           nativeQuery = true)
    List<Promotion> findPromotionsByService(@Param("serviceId") String serviceId);
    
    /**
     * Get promotions by product
     */
    @Query(value = "SELECT * FROM dev.promotions p WHERE p.is_deleted = false AND p.is_active = true " +
           "AND (p.target_products IS NULL OR p.target_products::text LIKE CONCAT('%', :productId, '%'))", 
           nativeQuery = true)
    List<Promotion> findPromotionsByProduct(@Param("productId") String productId);
    
    /**
     * Increment usage count for a promotion
     */
    @Modifying
    @Query("UPDATE Promotion p SET p.usedCount = p.usedCount + 1 WHERE p.promotionId = :promotionId")
    void incrementUsageCount(@Param("promotionId") UUID promotionId);
    
    /**
     * Get promotion usage statistics
     */
    @Query("SELECT SUM(p.usedCount) FROM Promotion p WHERE p.isDeleted = false")
    Long getTotalUsageCount();
    
    @Query("SELECT AVG(p.usedCount) FROM Promotion p WHERE p.isDeleted = false")
    Double getAverageUsageCount();
    
    @Query("SELECT MAX(p.usedCount) FROM Promotion p WHERE p.isDeleted = false")
    Integer getMaxUsageCount();
    
    /**
     * Find promotions that can be applied to an order
     */
    @Query("SELECT p FROM Promotion p WHERE p.isDeleted = false AND p.isActive = true " +
           "AND p.startDate <= :now AND p.endDate >= :now " +
           "AND (p.usageLimit IS NULL OR p.usedCount < p.usageLimit) " +
           "AND (p.minOrderAmount IS NULL OR p.minOrderAmount <= :orderAmount)")
    List<Promotion> findApplicablePromotions(@Param("now") LocalDateTime now,
                                             @Param("orderAmount") Double orderAmount);
    
    /**
     * Find promotions by multiple criteria
     */
    @Query("SELECT p FROM Promotion p WHERE p.isDeleted = false AND p.isActive = true " +
           "AND (:categoryId IS NULL OR p.category.categoryId = :categoryId) " +
           "AND (:promotionType IS NULL OR p.promotionType = :promotionType) " +
           "AND (:discountType IS NULL OR p.discountType = :discountType) " +
           "AND (:isVisible IS NULL OR p.isVisible = :isVisible) " +
           "AND (:autoApply IS NULL OR p.autoApply = :autoApply) " +
           "AND (:stackable IS NULL OR p.stackable = :stackable)")
    List<Promotion> findPromotionsByMultipleCriteria(@Param("categoryId") UUID categoryId,
                                                     @Param("promotionType") String promotionType,
                                                     @Param("discountType") Promotion.DiscountType discountType,
                                                     @Param("isVisible") Boolean isVisible,
                                                     @Param("autoApply") Boolean autoApply,
                                                     @Param("stackable") Boolean stackable);
    
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
     * Update promotion visibility
     */
    @Modifying
    @Query("UPDATE Promotion p SET p.isVisible = :isVisible WHERE p.promotionId = :promotionId")
    void updatePromotionVisibility(@Param("promotionId") UUID promotionId, @Param("isVisible") Boolean isVisible);
}
