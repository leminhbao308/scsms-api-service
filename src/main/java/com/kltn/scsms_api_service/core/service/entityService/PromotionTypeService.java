package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.PromotionType;
import com.kltn.scsms_api_service.core.repository.PromotionTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PromotionTypeService {
    
    private final PromotionTypeRepository promotionTypeRepository;
    
    /**
     * Find all promotion types
     */
    public List<PromotionType> findAll() {
        log.debug("Finding all promotion types");
        return promotionTypeRepository.findAll();
    }
    
    /**
     * Find all promotion types with pagination
     */
    public Page<PromotionType> findAll(Pageable pageable) {
        log.debug("Finding all promotion types with pagination: {}", pageable);
        return promotionTypeRepository.findAll(pageable);
    }
    
    /**
     * Find promotion type by ID
     */
    public Optional<PromotionType> findById(UUID promotionTypeId) {
        log.debug("Finding promotion type by ID: {}", promotionTypeId);
        return promotionTypeRepository.findById(promotionTypeId);
    }
    
    /**
     * Get promotion type by ID (throws exception if not found)
     */
    public PromotionType getById(UUID promotionTypeId) {
        log.debug("Getting promotion type by ID: {}", promotionTypeId);
        return promotionTypeRepository.findById(promotionTypeId)
                .orElseThrow(() -> new RuntimeException("PromotionType not found with ID: " + promotionTypeId));
    }
    
    /**
     * Find promotion type by type code
     */
    public Optional<PromotionType> findByTypeCode(String typeCode) {
        log.debug("Finding promotion type by type code: {}", typeCode);
        return promotionTypeRepository.findByTypeCode(typeCode);
    }
    
    /**
     * Check if promotion type exists by type code
     */
    public boolean existsByTypeCode(String typeCode) {
        log.debug("Checking if promotion type exists by type code: {}", typeCode);
        return promotionTypeRepository.existsByTypeCode(typeCode);
    }
    
    /**
     * Check if promotion type exists by type code excluding specific ID
     */
    public boolean existsByTypeCodeExcludingId(String typeCode, UUID excludeId) {
        log.debug("Checking if promotion type exists by type code: {} excluding ID: {}", typeCode, excludeId);
        return promotionTypeRepository.existsByTypeCodeExcludingId(typeCode, excludeId);
    }
    
    /**
     * Find all active promotion types
     */
    public List<PromotionType> findAllActive() {
        log.debug("Finding all active promotion types");
        return promotionTypeRepository.findAllActive();
    }
    
    /**
     * Find promotion types by name containing keyword
     */
    public List<PromotionType> findByTypeNameContaining(String keyword) {
        log.debug("Finding promotion types by name containing: {}", keyword);
        return promotionTypeRepository.findByTypeNameContainingIgnoreCase(keyword);
    }
    
    /**
     * Find promotion types by description containing keyword
     */
    public List<PromotionType> findByDescriptionContaining(String keyword) {
        log.debug("Finding promotion types by description containing: {}", keyword);
        return promotionTypeRepository.findByDescriptionContainingIgnoreCase(keyword);
    }
    
    /**
     * Search promotion types by keyword
     */
    public List<PromotionType> searchByKeyword(String keyword) {
        log.debug("Searching promotion types by keyword: {}", keyword);
        return promotionTypeRepository.searchByKeyword(keyword);
    }
    
    /**
     * Count active promotion types
     */
    public long countActive() {
        log.debug("Counting active promotion types");
        return promotionTypeRepository.countActive();
    }
    
    /**
     * Count all promotion types
     */
    public long countAll() {
        log.debug("Counting all promotion types");
        return promotionTypeRepository.countAll();
    }
    
    /**
     * Save promotion type
     */
    @Transactional
    public PromotionType save(PromotionType promotionType) {
        log.info("Saving promotion type: {}", promotionType.getTypeName());
        return promotionTypeRepository.save(promotionType);
    }
    
    /**
     * Update promotion type
     */
    @Transactional
    public PromotionType update(PromotionType promotionType) {
        log.info("Updating promotion type: {}", promotionType.getTypeName());
        return promotionTypeRepository.save(promotionType);
    }
    
    /**
     * Soft delete promotion type by ID
     */
    @Transactional
    public void softDeleteById(UUID promotionTypeId) {
        log.info("Soft deleting promotion type with ID: {}", promotionTypeId);
        PromotionType promotionType = getById(promotionTypeId);
        promotionType.setIsDeleted(true);
        promotionType.setIsActive(false);
        promotionTypeRepository.save(promotionType);
    }
    
    /**
     * Activate promotion type by ID
     */
    @Transactional
    public void activateById(UUID promotionTypeId) {
        log.info("Activating promotion type with ID: {}", promotionTypeId);
        PromotionType promotionType = getById(promotionTypeId);
        promotionType.setIsActive(true);
        promotionTypeRepository.save(promotionType);
    }
    
    /**
     * Deactivate promotion type by ID
     */
    @Transactional
    public void deactivateById(UUID promotionTypeId) {
        log.info("Deactivating promotion type with ID: {}", promotionTypeId);
        PromotionType promotionType = getById(promotionTypeId);
        promotionType.setIsActive(false);
        promotionTypeRepository.save(promotionType);
    }
}
