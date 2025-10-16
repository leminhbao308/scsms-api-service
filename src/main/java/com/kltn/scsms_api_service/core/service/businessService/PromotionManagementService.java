package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.promotionManagement.PromotionInfoDto;
import com.kltn.scsms_api_service.core.dto.promotionManagement.param.PromotionFilterParam;
import com.kltn.scsms_api_service.core.dto.promotionManagement.request.CreatePromotionRequest;
import com.kltn.scsms_api_service.core.dto.promotionManagement.request.UpdatePromotionRequest;
import com.kltn.scsms_api_service.core.dto.promotionManagement.request.UpdatePromotionStatusRequest;
import com.kltn.scsms_api_service.core.entity.Branch;
import com.kltn.scsms_api_service.core.entity.Promotion;
import com.kltn.scsms_api_service.core.service.entityService.BranchService;
import com.kltn.scsms_api_service.core.service.entityService.PromotionService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.PromotionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class PromotionManagementService {
    
    private final PromotionMapper promotionMapper;
    private final PromotionService promotionService;
    private final BranchService branchService;
    
    /**
     * Get all promotions with filters
     */
    public Page<PromotionInfoDto> getAllPromotions(PromotionFilterParam filterParam) {
        log.info("Getting all promotions with filters: {}", filterParam);
        
        Page<Promotion> promotionPage = promotionService.getAllPromotionsWithFilters(filterParam);
        
        return promotionPage.map(promotionMapper::toPromotionInfoDto);
    }
    
    /**
     * Get promotion by ID
     */
    public PromotionInfoDto getPromotionById(UUID promotionId) {
        log.info("Getting promotion by ID: {}", promotionId);
        
        Promotion promotion = promotionService.getById(promotionId);
        
        return promotionMapper.toPromotionInfoDto(promotion);
    }
    
    /**
     * Get promotion by code
     */
    public PromotionInfoDto getPromotionByCode(String promotionCode) {
        log.info("Getting promotion by code: {}", promotionCode);
        
        Promotion promotion = promotionService.findByPromotionCode(promotionCode)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND,
                "Promotion with code " + promotionCode + " not found."));
        
        return promotionMapper.toPromotionInfoDto(promotion);
    }
    
    /**
     * Create new promotion
     */
    @Transactional
    public PromotionInfoDto createPromotion(CreatePromotionRequest createPromotionRequest) {
        log.info("Creating new promotion with code: {}", createPromotionRequest.getPromotionCode());
        
        // Validate promotion code uniqueness
        if (promotionService.existsByPromotionCode(createPromotionRequest.getPromotionCode())) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                "Promotion code " + createPromotionRequest.getPromotionCode() + " already exists.");
        }
        
        // Validate branch exists if provided
        if (createPromotionRequest.getBranchId() != null) {
            Branch branch = branchService.findById(createPromotionRequest.getBranchId()).orElse(null);
            if (branch == null) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Branch with ID " + createPromotionRequest.getBranchId() + " not found.");
            }
        }
        
        // Create promotion entity
        Promotion newPromotion = promotionMapper.toEntity(createPromotionRequest);
        
        // Set related entities if provided
        setRelatedEntities(newPromotion, createPromotionRequest);
        
        // Save promotion
        Promotion createdPromotion = promotionService.savePromotion(newPromotion);
        
        log.info("Created new promotion with ID: {}", createdPromotion.getPromotionId());
        
        return promotionMapper.toPromotionInfoDto(createdPromotion);
    }
    
    /**
     * Update existing promotion
     */
    @Transactional
    public PromotionInfoDto updatePromotion(UUID promotionId, UpdatePromotionRequest updatePromotionRequest) {
        log.info("Updating promotion with ID: {}", promotionId);
        
        // Get existing promotion
        Promotion existingPromotion = promotionService.getById(promotionId);
        
        // Validate promotion code uniqueness if being updated
        if (updatePromotionRequest.getPromotionCode() != null &&
            !updatePromotionRequest.getPromotionCode().equals(existingPromotion.getPromotionCode())) {
            
            if (promotionService.existsByPromotionCodeAndPromotionIdNot(
                updatePromotionRequest.getPromotionCode(), promotionId)) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Promotion code " + updatePromotionRequest.getPromotionCode() + " already exists.");
            }
        }
        
        // Update promotion entity
        promotionMapper.updateEntityFromRequest(updatePromotionRequest, existingPromotion);
        
        // Update branch if provided
        if (updatePromotionRequest.getBranchId() != null) {
            Branch branch = branchService.findById(updatePromotionRequest.getBranchId()).orElse(null);
            if (branch == null) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "Branch with ID " + updatePromotionRequest.getBranchId() + " not found.");
            }
            existingPromotion.setBranch(branch);
        }
        
        // Update related entities if provided
        updateRelatedEntities(existingPromotion, updatePromotionRequest);
        
        // Save updated promotion
        Promotion updatedPromotion = promotionService.savePromotion(existingPromotion);
        
        log.info("Updated promotion with ID: {}", updatedPromotion.getPromotionId());
        
        return promotionMapper.toPromotionInfoDto(updatedPromotion);
    }
    
    /**
     * Delete promotion (soft delete)
     */
    @Transactional
    public void deletePromotion(UUID promotionId) {
        log.info("Deleting promotion with ID: {}", promotionId);
        
        // Check promotion exists
        Promotion existingPromotion = promotionService.getById(promotionId);
        
        // Check if promotion is being used
        if (existingPromotion.getUsages() != null && !existingPromotion.getUsages().isEmpty()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST,
                "Cannot delete promotion that has been used. Used count: " + existingPromotion.getUsages().size());
        }
        
        // Soft delete promotion
        promotionService.deletePromotion(existingPromotion);
        
        log.info("Deleted promotion with ID: {}", promotionId);
    }
    
    /**
     * Update promotion status (activate/deactivate)
     */
    @Transactional
    public void updatePromotionStatus(UUID promotionId, UpdatePromotionStatusRequest request) {
        log.info("Updating promotion status with ID: {} to active: {}", promotionId, request.getIsActive());
        
        promotionService.updatePromotionStatus(promotionId, request.getIsActive());
        
        log.info("Updated promotion status with ID: {} to active: {}", promotionId, request.getIsActive());
    }
    
    /**
     * Make promotion visible
     */
    @Transactional
    public void makePromotionVisible(UUID promotionId) {
        log.info("Making promotion visible with ID: {}", promotionId);
        
        promotionService.updatePromotionVisibility(promotionId, true);
        
        log.info("Made promotion visible with ID: {}", promotionId);
    }
    
    /**
     * Make promotion invisible
     */
    @Transactional
    public void makePromotionInvisible(UUID promotionId) {
        log.info("Making promotion invisible with ID: {}", promotionId);
        
        promotionService.updatePromotionVisibility(promotionId, false);
        
        log.info("Made promotion invisible with ID: {}", promotionId);
    }
    
    /**
     * Restore promotion (undo soft delete)
     */
    @Transactional
    public void restorePromotion(UUID promotionId) {
        log.info("Restoring promotion with ID: {}", promotionId);
        
        promotionService.restorePromotion(promotionId);
        
        log.info("Restored promotion with ID: {}", promotionId);
    }
    
    /**
     * Get promotion statistics
     */
    public PromotionStatisticsDto getPromotionStatistics() {
        log.info("Getting promotion statistics");
        
        return PromotionStatisticsDto.builder()
            .totalPromotions(promotionService.getTotalPromotionsCount())
            .activePromotions(promotionService.getActivePromotionsCount())
            .visiblePromotions(promotionService.getVisiblePromotionsCount())
            .autoApplyPromotions(promotionService.getAutoApplyPromotionsCount())
            .stackablePromotions(promotionService.getStackablePromotionsCount())
            .build();
    }
    
    /**
     * Get active promotions
     */
    public Page<PromotionInfoDto> getActivePromotions(PromotionFilterParam filterParam) {
        log.info("Getting active promotions");
        
        // Set filter to only show active promotions
        filterParam.setIsActive(true);
        filterParam.setIsExpired(false);
        
        return getAllPromotions(filterParam);
    }
    
    /**
     * Get visible promotions
     */
    public Page<PromotionInfoDto> getVisiblePromotions(PromotionFilterParam filterParam) {
        log.info("Getting visible promotions");
        
        // Set filter to only show visible promotions
        filterParam.setIsActive(true);
        filterParam.setIsExpired(false);
        
        return getAllPromotions(filterParam);
    }
    
    /**
     * Get expired promotions
     */
    public Page<PromotionInfoDto> getExpiredPromotions(PromotionFilterParam filterParam) {
        log.info("Getting expired promotions");
        
        // Set filter to only show expired promotions
        filterParam.setIsExpired(true);
        
        return getAllPromotions(filterParam);
    }
    
    /**
     * Get promotions starting soon
     */
    public Page<PromotionInfoDto> getPromotionsStartingSoon(PromotionFilterParam filterParam) {
        log.info("Getting promotions starting soon");
        
        // Set filter to show promotions starting soon
        filterParam.setIsStartingSoon(true);
        
        return getAllPromotions(filterParam);
    }
    
    /**
     * Get promotions ending soon
     */
    public Page<PromotionInfoDto> getPromotionsEndingSoon(PromotionFilterParam filterParam) {
        log.info("Getting promotions ending soon");
        
        // Set filter to show promotions ending soon
        filterParam.setIsEndingSoon(true);
        
        return getAllPromotions(filterParam);
    }
    
    /**
     * Set related entities for new promotion
     */
    private void setRelatedEntities(Promotion promotion, CreatePromotionRequest request) {
        // Set branch if provided
        if (request.getBranchId() != null) {
            Branch branch = branchService.findById(request.getBranchId()).orElse(null);
            if (branch != null) {
                promotion.setBranch(branch);
            }
        }
        
        // TODO: Handle promotion lines if provided
        // This will be implemented when we add promotion line management
    }
    
    /**
     * Update related entities for existing promotion
     */
    private void updateRelatedEntities(Promotion promotion, UpdatePromotionRequest request) {
        // Update branch if provided
        if (request.getBranchId() != null) {
            Branch branch = branchService.findById(request.getBranchId()).orElse(null);
            if (branch != null) {
                promotion.setBranch(branch);
            }
        }
        
        // TODO: Handle promotion lines if provided
        // This will be implemented when we add promotion line management
    }
    
    /**
     * Promotion statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PromotionStatisticsDto {
        private Long totalPromotions;
        private Long activePromotions;
        private Long visiblePromotions;
        private Long autoApplyPromotions;
        private Long stackablePromotions;
    }
}
