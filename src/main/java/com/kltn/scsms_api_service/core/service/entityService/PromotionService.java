package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.dto.promotionManagement.param.PromotionFilterParam;
import com.kltn.scsms_api_service.core.entity.Promotion;
import com.kltn.scsms_api_service.core.repository.PromotionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionService {
    
    private final PromotionRepository promotionRepository;
    private final EntityManager entityManager;
    
    /**
     * Find promotion by ID
     */
    public Optional<Promotion> findById(UUID promotionId) {
        return promotionRepository.findById(promotionId);
    }
    
    /**
     * Get promotion by ID (required)
     */
    public Promotion getById(UUID promotionId) {
        return promotionRepository.getById(promotionId);
    }
    
    /**
     * Find promotion by code
     */
    public Optional<Promotion> findByPromotionCode(String promotionCode) {
        return promotionRepository.findByPromotionCode(promotionCode);
    }
    
    /**
     * Get promotion reference by ID (for lazy loading)
     */
    public Promotion getReferenceById(UUID promotionId) {
        return promotionRepository.getReferenceById(promotionId);
    }
    
    /**
     * Check if promotion code exists
     */
    public boolean existsByPromotionCode(String promotionCode) {
        return promotionRepository.existsByPromotionCode(promotionCode);
    }
    
    /**
     * Check if promotion code exists, excluding specific promotion ID
     */
    public boolean existsByPromotionCodeAndPromotionIdNot(String promotionCode, UUID promotionId) {
        return promotionRepository.existsByPromotionCodeAndPromotionIdNot(promotionCode, promotionId);
    }
    
    /**
     * Save promotion
     */
    public Promotion savePromotion(Promotion promotion) {
        return promotionRepository.save(promotion);
    }
    
    /**
     * Delete promotion (soft delete)
     */
    public void deletePromotion(Promotion promotion) {
        promotion.setIsDeleted(true);
        promotionRepository.save(promotion);
    }
    
    /**
     * Soft delete promotion by ID
     */
    public void softDeletePromotion(UUID promotionId) {
        promotionRepository.softDeletePromotion(promotionId);
    }
    
    /**
     * Restore promotion (undo soft delete)
     */
    public void restorePromotion(UUID promotionId) {
        promotionRepository.restorePromotion(promotionId);
    }
    
    /**
     * Update promotion status
     */
    public void updatePromotionStatus(UUID promotionId, Boolean isActive) {
        promotionRepository.updatePromotionStatus(promotionId, isActive);
    }
    
    /**
     * Update promotion visibility
     */
    public void updatePromotionVisibility(UUID promotionId, Boolean isVisible) {
        promotionRepository.updatePromotionVisibility(promotionId, isVisible);
    }
    
    
    /**
     * Get all promotions with filters
     */
    public Page<Promotion> getAllPromotionsWithFilters(PromotionFilterParam filterParam) {
        log.info("Getting promotions with filters: {}", filterParam);
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Promotion> query = cb.createQuery(Promotion.class);
        Root<Promotion> promotionRoot = query.from(Promotion.class);
        
        // Join with PromotionType entity for promotion type-based filtering
        Join<Object, Object> promotionTypeJoin = promotionRoot.join("promotionType", JoinType.LEFT);
        
        // Join with Branch entity for branch-based filtering
        Join<Object, Object> branchJoin = promotionRoot.join("branch", JoinType.LEFT);
        
        List<Predicate> predicates = buildPredicates(cb, promotionRoot, promotionTypeJoin, 
                                                   branchJoin, filterParam);
        
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        // Apply sorting
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(filterParam.getDirection())
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Order order;
        if ("promotionType.typeName".equals(filterParam.getSort())) {
            order = sortDirection == Sort.Direction.ASC
                ? cb.asc(promotionTypeJoin.get("typeName"))
                : cb.desc(promotionTypeJoin.get("typeName"));
        } else if ("branch.branchName".equals(filterParam.getSort())) {
            order = sortDirection == Sort.Direction.ASC
                ? cb.asc(branchJoin.get("branchName"))
                : cb.desc(branchJoin.get("branchName"));
        } else {
            order = sortDirection == Sort.Direction.ASC
                ? cb.asc(promotionRoot.get(filterParam.getSort()))
                : cb.desc(promotionRoot.get(filterParam.getSort()));
        }
        query.orderBy(order);
        
        TypedQuery<Promotion> typedQuery = entityManager.createQuery(query);
        
        // Get total count for pagination
        long totalElements = getTotalCount(filterParam);
        
        // Apply pagination
        int offset = filterParam.getPage() * filterParam.getSize();
        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(filterParam.getSize());
        
        List<Promotion> promotions = typedQuery.getResultList();
        
        PageRequest pageRequest = PageRequest.of(filterParam.getPage(), filterParam.getSize());
        return new PageImpl<>(promotions, pageRequest, totalElements);
    }
    
    /**
     * Build predicates for filtering
     */
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Promotion> promotionRoot,
                                          Join<Object, Object> promotionTypeJoin,
                                          Join<Object, Object> branchJoin,
                                          PromotionFilterParam filterParam) {
        List<Predicate> predicates = new ArrayList<>();
        
        // Basic filters
        if (filterParam.getPromotionCode() != null) {
            predicates.add(cb.like(cb.lower(promotionRoot.get("promotionCode")),
                "%" + filterParam.getPromotionCode().toLowerCase() + "%"));
        }
        
        if (filterParam.getName() != null) {
            predicates.add(cb.like(cb.lower(promotionRoot.get("name")),
                "%" + filterParam.getName().toLowerCase() + "%"));
        }
        
        if (filterParam.getPromotionTypeId() != null) {
            predicates.add(cb.equal(promotionTypeJoin.get("promotionTypeId"), filterParam.getPromotionTypeId()));
        }
        
        if (filterParam.getBranchId() != null) {
            predicates.add(cb.equal(branchJoin.get("branchId"), filterParam.getBranchId()));
        }
        
        if (filterParam.getIsStackable() != null) {
            predicates.add(cb.equal(promotionRoot.get("isStackable"), filterParam.getIsStackable()));
        }
        
        if (filterParam.getCouponRedeemOnce() != null) {
            predicates.add(cb.equal(promotionRoot.get("couponRedeemOnce"), filterParam.getCouponRedeemOnce()));
        }
        
        if (filterParam.getIsActive() != null) {
            predicates.add(cb.equal(promotionRoot.get("isActive"), filterParam.getIsActive()));
        }
        
        
        // Usage filters
        if (filterParam.getHasUsageLimit() != null) {
            if (filterParam.getHasUsageLimit()) {
                predicates.add(cb.isNotNull(promotionRoot.get("usageLimit")));
            } else {
                predicates.add(cb.isNull(promotionRoot.get("usageLimit")));
            }
        }
        
        if (filterParam.getUsageLimitExceeded() != null) {
            if (filterParam.getUsageLimitExceeded()) {
                predicates.add(cb.and(
                    cb.isNotNull(promotionRoot.get("usageLimit")),
                    cb.greaterThanOrEqualTo(cb.size(promotionRoot.get("usages")), promotionRoot.get("usageLimit"))
                ));
            } else {
                predicates.add(cb.or(
                    cb.isNull(promotionRoot.get("usageLimit")),
                    cb.lessThan(cb.size(promotionRoot.get("usages")), promotionRoot.get("usageLimit"))
                ));
            }
        }
        
        if (filterParam.getMinUsageCount() != null) {
            predicates.add(cb.greaterThanOrEqualTo(cb.size(promotionRoot.get("usages")), filterParam.getMinUsageCount()));
        }
        
        if (filterParam.getMaxUsageCount() != null) {
            predicates.add(cb.lessThanOrEqualTo(cb.size(promotionRoot.get("usages")), filterParam.getMaxUsageCount()));
        }
        
        // Date range filters
        addDateRangePredicates(cb, promotionRoot, predicates, filterParam);
        
        // Status filters
        addStatusPredicates(cb, promotionRoot, predicates, filterParam);
        
        // Priority filters
        if (filterParam.getMinPriority() != null) {
            predicates.add(cb.greaterThanOrEqualTo(promotionRoot.get("priority"), filterParam.getMinPriority()));
        }
        
        if (filterParam.getMaxPriority() != null) {
            predicates.add(cb.lessThanOrEqualTo(promotionRoot.get("priority"), filterParam.getMaxPriority()));
        }
        
        // Search filter
        if (filterParam.getSearch() != null) {
            String searchPattern = "%" + filterParam.getSearch().toLowerCase() + "%";
            Predicate searchPredicate = cb.or(
                cb.like(cb.lower(promotionRoot.get("name")), searchPattern),
                cb.like(cb.lower(promotionRoot.get("promotionCode")), searchPattern),
                cb.like(cb.lower(promotionRoot.get("description")), searchPattern)
            );
            predicates.add(searchPredicate);
        }
        
        return predicates;
    }
    
    /**
     * Add date range predicates
     */
    private void addDateRangePredicates(CriteriaBuilder cb, Root<Promotion> promotionRoot,
                                      List<Predicate> predicates, PromotionFilterParam filterParam) {
        // Start date range
        if (filterParam.getStartAtFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(promotionRoot.get("startAt"), filterParam.getStartAtFrom()));
        }
        if (filterParam.getStartAtTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(promotionRoot.get("startAt"), filterParam.getStartAtTo()));
        }
        
        // End date range
        if (filterParam.getEndAtFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(promotionRoot.get("endAt"), filterParam.getEndAtFrom()));
        }
        if (filterParam.getEndAtTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(promotionRoot.get("endAt"), filterParam.getEndAtTo()));
        }
        
        // Created date range
        if (filterParam.getCreatedDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(promotionRoot.get("createdDate"), filterParam.getCreatedDateFrom()));
        }
        if (filterParam.getCreatedDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(promotionRoot.get("createdDate"), filterParam.getCreatedDateTo()));
        }
        
        // Modified date range
        if (filterParam.getModifiedDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(promotionRoot.get("modifiedDate"), filterParam.getModifiedDateFrom()));
        }
        if (filterParam.getModifiedDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(promotionRoot.get("modifiedDate"), filterParam.getModifiedDateTo()));
        }
    }
    
    /**
     * Add status predicates
     */
    private void addStatusPredicates(CriteriaBuilder cb, Root<Promotion> promotionRoot,
                                   List<Predicate> predicates, PromotionFilterParam filterParam) {
        LocalDateTime now = LocalDateTime.now();
        
        if (filterParam.getIsExpired() != null) {
            if (filterParam.getIsExpired()) {
                predicates.add(cb.lessThan(promotionRoot.get("endAt"), now));
            } else {
                predicates.add(cb.greaterThanOrEqualTo(promotionRoot.get("endAt"), now));
            }
        }
        
        if (filterParam.getIsAvailable() != null) {
            if (filterParam.getIsAvailable()) {
                predicates.add(cb.and(
                    cb.equal(promotionRoot.get("isActive"), true),
                    cb.equal(promotionRoot.get("isDeleted"), false),
                    cb.or(
                        cb.isNull(promotionRoot.get("startAt")),
                        cb.lessThanOrEqualTo(promotionRoot.get("startAt"), now)
                    ),
                    cb.or(
                        cb.isNull(promotionRoot.get("endAt")),
                        cb.greaterThanOrEqualTo(promotionRoot.get("endAt"), now)
                    ),
                    cb.or(
                        cb.isNull(promotionRoot.get("usageLimit")),
                        cb.lessThan(cb.size(promotionRoot.get("usages")), promotionRoot.get("usageLimit"))
                    )
                ));
            } else {
                predicates.add(cb.or(
                    cb.equal(promotionRoot.get("isActive"), false),
                    cb.equal(promotionRoot.get("isDeleted"), true),
                    cb.and(
                        cb.isNotNull(promotionRoot.get("startAt")),
                        cb.greaterThan(promotionRoot.get("startAt"), now)
                    ),
                    cb.and(
                        cb.isNotNull(promotionRoot.get("endAt")),
                        cb.lessThan(promotionRoot.get("endAt"), now)
                    ),
                    cb.and(
                        cb.isNotNull(promotionRoot.get("usageLimit")),
                        cb.greaterThanOrEqualTo(cb.size(promotionRoot.get("usages")), promotionRoot.get("usageLimit"))
                    )
                ));
            }
        }
        
        if (filterParam.getIsStartingSoon() != null && filterParam.getIsStartingSoon()) {
            LocalDateTime futureTime = now.plusDays(7); // Starting within 7 days
            predicates.add(cb.and(
                cb.greaterThan(promotionRoot.get("startAt"), now),
                cb.lessThanOrEqualTo(promotionRoot.get("startAt"), futureTime)
            ));
        }
        
        if (filterParam.getIsEndingSoon() != null && filterParam.getIsEndingSoon()) {
            LocalDateTime futureTime = now.plusDays(7); // Ending within 7 days
            predicates.add(cb.and(
                cb.greaterThan(promotionRoot.get("endAt"), now),
                cb.lessThanOrEqualTo(promotionRoot.get("endAt"), futureTime)
            ));
        }
    }
    
    /**
     * Get total count for pagination
     */
    private long getTotalCount(PromotionFilterParam filterParam) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Promotion> promotionRoot = countQuery.from(Promotion.class);
        Join<Object, Object> promotionTypeJoin = promotionRoot.join("promotionType", JoinType.LEFT);
        Join<Object, Object> branchJoin = promotionRoot.join("branch", JoinType.LEFT);
        
        countQuery.select(cb.count(promotionRoot));
        
        List<Predicate> predicates = buildPredicates(cb, promotionRoot, promotionTypeJoin,
                                                   branchJoin, filterParam);
        if (!predicates.isEmpty()) {
            countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        return entityManager.createQuery(countQuery).getSingleResult();
    }
    
    /**
     * Get active promotions
     */
    public List<Promotion> getActivePromotions() {
        return promotionRepository.findActivePromotions(LocalDateTime.now());
    }
    
    /**
     * Get visible promotions
     */
    public List<Promotion> getVisiblePromotions() {
        return promotionRepository.findActivePromotions(LocalDateTime.now());
    }
    
    /**
     * Get auto-apply promotions
     */
    public List<Promotion> getAutoApplyPromotions() {
        return promotionRepository.findAutoApplyPromotions(LocalDateTime.now());
    }
    
    /**
     * Get expired promotions
     */
    public List<Promotion> getExpiredPromotions() {
        return promotionRepository.findExpiredPromotions(LocalDateTime.now());
    }
    
    /**
     * Get promotions starting soon
     */
    public List<Promotion> getPromotionsStartingSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureTime = now.plusDays(7);
        return promotionRepository.findPromotionsStartingSoon(now, futureTime);
    }
    
    /**
     * Get promotions ending soon
     */
    public List<Promotion> getPromotionsEndingSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureTime = now.plusDays(7);
        return promotionRepository.findPromotionsEndingSoon(now, futureTime);
    }
    
    /**
     * Get most used promotions
     */
    public List<Promotion> getMostUsedPromotions(int limit) {
        return promotionRepository.findMostUsedPromotions(PageRequest.of(0, limit));
    }
    
    /**
     * Get least used promotions
     */
    public List<Promotion> getLeastUsedPromotions(int limit) {
        return promotionRepository.findLeastUsedPromotions(PageRequest.of(0, limit));
    }
    
    /**
     * Get promotion statistics
     */
    public long getTotalPromotionsCount() {
        return promotionRepository.getTotalPromotionsCount();
    }
    
    public long getActivePromotionsCount() {
        return promotionRepository.getActivePromotionsCount();
    }
    
    public long getVisiblePromotionsCount() {
        return promotionRepository.getVisiblePromotionsCount();
    }
    
    public long getAutoApplyPromotionsCount() {
        return promotionRepository.getAutoApplyPromotionsCount();
    }
    
    public long getStackablePromotionsCount() {
        return promotionRepository.getStackablePromotionsCount();
    }
}
