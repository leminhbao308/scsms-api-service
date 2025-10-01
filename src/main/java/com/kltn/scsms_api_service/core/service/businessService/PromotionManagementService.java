package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.promotionManagement.PromotionInfoDto;
import com.kltn.scsms_api_service.core.dto.promotionManagement.param.PromotionFilterParam;
import com.kltn.scsms_api_service.core.dto.promotionManagement.request.CreatePromotionRequest;
import com.kltn.scsms_api_service.core.dto.promotionManagement.request.UpdatePromotionRequest;
import com.kltn.scsms_api_service.core.entity.Category;
import com.kltn.scsms_api_service.core.entity.Product;
import com.kltn.scsms_api_service.core.entity.Promotion;
import com.kltn.scsms_api_service.core.entity.Service;
import com.kltn.scsms_api_service.core.service.entityService.CategoryService;
import com.kltn.scsms_api_service.core.service.entityService.ProductService;
import com.kltn.scsms_api_service.core.service.entityService.PromotionService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceService;
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
    private final CategoryService categoryService;
    private final ProductService productService;
    private final ServiceService serviceService;
    
    /**
     * Get all promotions with filters
     */
    public Page<PromotionInfoDto> getAllPromotions(PromotionFilterParam filterParam) {
        log.info("Getting all promotions with filters: {}", filterParam);
        
        Page<Promotion> promotionPage = promotionService.getAllPromotionsWithFilters(filterParam);
        
        return promotionPage.map(promotionMapper::toDetailedInfoDto);
    }
    
    /**
     * Get promotion by ID
     */
    public PromotionInfoDto getPromotionById(UUID promotionId) {
        log.info("Getting promotion by ID: {}", promotionId);
        
        Promotion promotion = promotionService.getById(promotionId);
        
        return promotionMapper.toDetailedInfoDto(promotion);
    }
    
    /**
     * Get promotion by code
     */
    public PromotionInfoDto getPromotionByCode(String promotionCode) {
        log.info("Getting promotion by code: {}", promotionCode);
        
        Promotion promotion = promotionService.findByPromotionCode(promotionCode)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, 
                "Promotion with code " + promotionCode + " not found."));
        
        return promotionMapper.toDetailedInfoDto(promotion);
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
        
        // Validate category exists
        Category category = categoryService.getById(createPromotionRequest.getCategoryId());
        if (category == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                "Category with ID " + createPromotionRequest.getCategoryId() + " not found.");
        }
        
        // Create promotion entity
        Promotion newPromotion = promotionMapper.toEntity(createPromotionRequest);
        
        // Set category
        newPromotion.setCategory(category);
        
        // Set related entities if provided
        setRelatedEntities(newPromotion, createPromotionRequest);
        
        // Save promotion
        Promotion createdPromotion = promotionService.savePromotion(newPromotion);
        
        log.info("Created new promotion with ID: {}", createdPromotion.getPromotionId());
        
        return promotionMapper.toDetailedInfoDto(createdPromotion);
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
        
        // Update category if provided
        if (updatePromotionRequest.getCategoryId() != null) {
            Category category = categoryService.getById(updatePromotionRequest.getCategoryId());
            if (category == null) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Category with ID " + updatePromotionRequest.getCategoryId() + " not found.");
            }
            existingPromotion.setCategory(category);
        }
        
        // Update related entities if provided
        updateRelatedEntities(existingPromotion, updatePromotionRequest);
        
        // Save updated promotion
        Promotion updatedPromotion = promotionService.savePromotion(existingPromotion);
        
        log.info("Updated promotion with ID: {}", updatedPromotion.getPromotionId());
        
        return promotionMapper.toDetailedInfoDto(updatedPromotion);
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
        if (existingPromotion.getUsedCount() > 0) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                "Cannot delete promotion that has been used. Used count: " + existingPromotion.getUsedCount());
        }
        
        // Soft delete promotion
        promotionService.deletePromotion(existingPromotion);
        
        log.info("Deleted promotion with ID: {}", promotionId);
    }
    
    /**
     * Activate promotion
     */
    @Transactional
    public void activatePromotion(UUID promotionId) {
        log.info("Activating promotion with ID: {}", promotionId);
        
        promotionService.updatePromotionStatus(promotionId, true);
        
        log.info("Activated promotion with ID: {}", promotionId);
    }
    
    /**
     * Deactivate promotion
     */
    @Transactional
    public void deactivatePromotion(UUID promotionId) {
        log.info("Deactivating promotion with ID: {}", promotionId);
        
        promotionService.updatePromotionStatus(promotionId, false);
        
        log.info("Deactivated promotion with ID: {}", promotionId);
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
        filterParam.setIsVisible(true);
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
        // Set free product if provided
        if (request.getFreeProductId() != null) {
            Product freeProduct = productService.getById(request.getFreeProductId());
            if (freeProduct == null) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Free product with ID " + request.getFreeProductId() + " not found.");
            }
            promotion.setFreeProduct(freeProduct);
        }
        
        // Set free service if provided
        if (request.getFreeServiceId() != null) {
            Service freeService = serviceService.getById(request.getFreeServiceId());
            if (freeService == null) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Free service with ID " + request.getFreeServiceId() + " not found.");
            }
            promotion.setFreeService(freeService);
        }
        
        // Set buy product if provided
        if (request.getBuyProductId() != null) {
            Product buyProduct = productService.getById(request.getBuyProductId());
            if (buyProduct == null) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Buy product with ID " + request.getBuyProductId() + " not found.");
            }
            promotion.setBuyProduct(buyProduct);
        }
        
        // Set get product if provided
        if (request.getGetProductId() != null) {
            Product getProduct = productService.getById(request.getGetProductId());
            if (getProduct == null) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Get product with ID " + request.getGetProductId() + " not found.");
            }
            promotion.setGetProduct(getProduct);
        }
    }
    
    /**
     * Update related entities for existing promotion
     */
    private void updateRelatedEntities(Promotion promotion, UpdatePromotionRequest request) {
        // Update free product if provided
        if (request.getFreeProductId() != null) {
            Product freeProduct = productService.getById(request.getFreeProductId());
            if (freeProduct == null) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Free product with ID " + request.getFreeProductId() + " not found.");
            }
            promotion.setFreeProduct(freeProduct);
        }
        
        // Update free service if provided
        if (request.getFreeServiceId() != null) {
            Service freeService = serviceService.getById(request.getFreeServiceId());
            if (freeService == null) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Free service with ID " + request.getFreeServiceId() + " not found.");
            }
            promotion.setFreeService(freeService);
        }
        
        // Update buy product if provided
        if (request.getBuyProductId() != null) {
            Product buyProduct = productService.getById(request.getBuyProductId());
            if (buyProduct == null) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Buy product with ID " + request.getBuyProductId() + " not found.");
            }
            promotion.setBuyProduct(buyProduct);
        }
        
        // Update get product if provided
        if (request.getGetProductId() != null) {
            Product getProduct = productService.getById(request.getGetProductId());
            if (getProduct == null) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Get product with ID " + request.getGetProductId() + " not found.");
            }
            promotion.setGetProduct(getProduct);
        }
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
