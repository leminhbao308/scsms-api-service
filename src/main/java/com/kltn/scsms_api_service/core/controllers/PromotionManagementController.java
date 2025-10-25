package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.promotionManagement.PromotionInfoDto;
import com.kltn.scsms_api_service.core.dto.promotionManagement.PromotionUsageHistoryDto;
import com.kltn.scsms_api_service.core.dto.promotionManagement.param.PromotionFilterParam;
import com.kltn.scsms_api_service.core.dto.promotionManagement.param.PromotionUsageHistoryFilterParam;
import com.kltn.scsms_api_service.core.dto.promotionManagement.request.CreatePromotionRequest;
import com.kltn.scsms_api_service.core.dto.promotionManagement.request.UpdatePromotionRequest;
import com.kltn.scsms_api_service.core.dto.promotionManagement.request.UpdatePromotionStatusRequest;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.response.PaginatedResponse;
import com.kltn.scsms_api_service.core.service.businessService.PromotionManagementService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller handling promotion management operations
 * Manages promotion creation, updates, deletion, and retrieval
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Promotion Management", description = "Promotion management endpoints")
public class PromotionManagementController {
    
    private final PromotionManagementService promotionManagementService;
    
    /**
     * Get all promotions with filters
     */
    @GetMapping(ApiConstant.GET_ALL_PROMOTIONS_API)
    @SwaggerOperation(summary = "Get all promotions", description = "Retrieve a paginated list of all promotions that can be filtered by various criteria")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_READ)
    public ResponseEntity<ApiResponse<PaginatedResponse<PromotionInfoDto>>> getAllPromotions(
        @ModelAttribute PromotionFilterParam promotionFilterParam) {
        log.info("Fetching all promotions");
        
        Page<PromotionInfoDto> promotions = promotionManagementService.getAllPromotions(
            PromotionFilterParam.standardize(promotionFilterParam));
        
        return ResponseBuilder.paginated("Promotions fetched successfully", promotions);
    }
    
    /**
     * Get promotion by ID
     */
    @GetMapping(ApiConstant.GET_PROMOTION_BY_ID_API)
    @SwaggerOperation(summary = "Get promotion by ID", description = "Retrieve a specific promotion by its ID")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_READ)
    public ResponseEntity<ApiResponse<PromotionInfoDto>> getPromotionById(
        @PathVariable(value = "promotionId") String promotionId) {
        log.info("Fetching promotion by ID: {}", promotionId);
        
        PromotionInfoDto promotion = promotionManagementService.getPromotionById(UUID.fromString(promotionId));
        
        return ResponseBuilder.success("Promotion fetched successfully", promotion);
    }
    
    /**
     * Get promotion by code
     */
    @GetMapping(ApiConstant.GET_PROMOTION_BY_CODE_API)
    @SwaggerOperation(summary = "Get promotion by code", description = "Retrieve a specific promotion by its code")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_READ)
    public ResponseEntity<ApiResponse<PromotionInfoDto>> getPromotionByCode(
        @PathVariable(value = "promotionCode") String promotionCode) {
        log.info("Fetching promotion by code: {}", promotionCode);
        
        PromotionInfoDto promotion = promotionManagementService.getPromotionByCode(promotionCode);
        
        return ResponseBuilder.success("Promotion fetched successfully", promotion);
    }
    
    /**
     * Create new promotion
     */
    @PostMapping(ApiConstant.CREATE_PROMOTION_API)
    @SwaggerOperation(summary = "Create a new promotion", description = "Create a new promotion with the provided details")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_CREATE)
    public ResponseEntity<ApiResponse<PromotionInfoDto>> createPromotion(
        @RequestBody CreatePromotionRequest createPromotionRequest) {
        log.info("Creating new promotion with code: {}", createPromotionRequest.getPromotionCode());
        
        PromotionInfoDto createdPromotion = promotionManagementService.createPromotion(createPromotionRequest);
        
        return ResponseBuilder.success("Promotion created successfully", createdPromotion);
    }
    
    /**
     * Update existing promotion
     */
    @PostMapping(ApiConstant.UPDATE_PROMOTION_API)
    @SwaggerOperation(summary = "Update an existing promotion", description = "Update the details of an existing promotion")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_UPDATE)
    public ResponseEntity<ApiResponse<PromotionInfoDto>> updatePromotion(
        @PathVariable(value = "promotionId") String promotionId,
        @RequestBody UpdatePromotionRequest updatePromotionRequest) {
        log.info("Updating promotion with ID: {}", promotionId);
        
        PromotionInfoDto updatedPromotion = promotionManagementService.updatePromotion(
            UUID.fromString(promotionId), updatePromotionRequest);
        
        return ResponseBuilder.success("Promotion updated successfully", updatedPromotion);
    }
    
    /**
     * Delete promotion (soft delete)
     */
    @PostMapping(ApiConstant.DELETE_PROMOTION_API)
    @SwaggerOperation(summary = "Delete a promotion", description = "Delete a promotion by its ID (soft delete)")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_DELETE)
    public ResponseEntity<ApiResponse<Void>> deletePromotion(
        @PathVariable(value = "promotionId") String promotionId) {
        log.info("Deleting promotion with ID: {}", promotionId);
        
        promotionManagementService.deletePromotion(UUID.fromString(promotionId));
        
        return ResponseBuilder.success("Promotion deleted successfully");
    }
    
    /**
     * Update promotion status (activate/deactivate)
     */
    @PostMapping(ApiConstant.UPDATE_PROMOTION_STATUS_API)
    @SwaggerOperation(summary = "Update promotion status", description = "Update promotion status to active or inactive")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_UPDATE)
    public ResponseEntity<ApiResponse<Void>> updatePromotionStatus(
        @PathVariable(value = "promotionId") String promotionId,
        @RequestBody UpdatePromotionStatusRequest request) {
        log.info("Updating promotion status with ID: {} to active: {}", promotionId, request.getIsActive());
        
        promotionManagementService.updatePromotionStatus(UUID.fromString(promotionId), request);
        
        String statusMessage = request.getIsActive() ? "activated" : "deactivated";
        return ResponseBuilder.success("Promotion " + statusMessage + " successfully");
    }
    
    /**
     * Make promotion visible
     */
    @PostMapping(ApiConstant.MAKE_PROMOTION_VISIBLE_API)
    @SwaggerOperation(summary = "Make promotion visible", description = "Make a promotion visible to customers")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_UPDATE)
    public ResponseEntity<ApiResponse<Void>> makePromotionVisible(
        @PathVariable(value = "promotionId") String promotionId) {
        log.info("Making promotion visible with ID: {}", promotionId);
        
        promotionManagementService.makePromotionVisible(UUID.fromString(promotionId));
        
        return ResponseBuilder.success("Promotion made visible successfully");
    }
    
    /**
     * Make promotion invisible
     */
    @PostMapping(ApiConstant.MAKE_PROMOTION_INVISIBLE_API)
    @SwaggerOperation(summary = "Make promotion invisible", description = "Make a promotion invisible to customers")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_UPDATE)
    public ResponseEntity<ApiResponse<Void>> makePromotionInvisible(
        @PathVariable(value = "promotionId") String promotionId) {
        log.info("Making promotion invisible with ID: {}", promotionId);
        
        promotionManagementService.makePromotionInvisible(UUID.fromString(promotionId));
        
        return ResponseBuilder.success("Promotion made invisible successfully");
    }
    
    /**
     * Restore promotion (undo soft delete)
     */
    @PostMapping(ApiConstant.RESTORE_PROMOTION_API)
    @SwaggerOperation(summary = "Restore a promotion", description = "Restore a soft-deleted promotion")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_UPDATE)
    public ResponseEntity<ApiResponse<Void>> restorePromotion(
        @PathVariable(value = "promotionId") String promotionId) {
        log.info("Restoring promotion with ID: {}", promotionId);
        
        promotionManagementService.restorePromotion(UUID.fromString(promotionId));
        
        return ResponseBuilder.success("Promotion restored successfully");
    }
    
    /**
     * Get promotion statistics
     */
    @GetMapping(ApiConstant.GET_PROMOTION_STATISTICS_API)
    @SwaggerOperation(summary = "Get promotion statistics", description = "Get statistics about promotions")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_READ)
    public ResponseEntity<ApiResponse<PromotionManagementService.PromotionStatisticsDto>> getPromotionStatistics() {
        log.info("Fetching promotion statistics");
        
        PromotionManagementService.PromotionStatisticsDto statistics = promotionManagementService
            .getPromotionStatistics();
        
        return ResponseBuilder.success("Promotion statistics fetched successfully", statistics);
    }
    
    /**
     * Get active promotions
     */
    @GetMapping(ApiConstant.GET_ACTIVE_PROMOTIONS_API)
    @SwaggerOperation(summary = "Get active promotions", description = "Retrieve a paginated list of active promotions")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_READ)
    public ResponseEntity<ApiResponse<PaginatedResponse<PromotionInfoDto>>> getActivePromotions(
        @ModelAttribute PromotionFilterParam promotionFilterParam) {
        log.info("Fetching active promotions");
        
        Page<PromotionInfoDto> promotions = promotionManagementService.getActivePromotions(
            PromotionFilterParam.standardize(promotionFilterParam));
        
        return ResponseBuilder.paginated("Active promotions fetched successfully", promotions);
    }
    
    /**
     * Get visible promotions
     */
    @GetMapping(ApiConstant.GET_VISIBLE_PROMOTIONS_API)
    @SwaggerOperation(summary = "Get visible promotions", description = "Retrieve a paginated list of visible promotions")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_READ)
    public ResponseEntity<ApiResponse<PaginatedResponse<PromotionInfoDto>>> getVisiblePromotions(
        @ModelAttribute PromotionFilterParam promotionFilterParam) {
        log.info("Fetching visible promotions");
        
        Page<PromotionInfoDto> promotions = promotionManagementService.getVisiblePromotions(
            PromotionFilterParam.standardize(promotionFilterParam));
        
        return ResponseBuilder.paginated("Visible promotions fetched successfully", promotions);
    }
    
    /**
     * Get expired promotions
     */
    @GetMapping(ApiConstant.GET_EXPIRED_PROMOTIONS_API)
    @SwaggerOperation(summary = "Get expired promotions", description = "Retrieve a paginated list of expired promotions")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_READ)
    public ResponseEntity<ApiResponse<PaginatedResponse<PromotionInfoDto>>> getExpiredPromotions(
        @ModelAttribute PromotionFilterParam promotionFilterParam) {
        log.info("Fetching expired promotions");
        
        Page<PromotionInfoDto> promotions = promotionManagementService.getExpiredPromotions(
            PromotionFilterParam.standardize(promotionFilterParam));
        
        return ResponseBuilder.paginated("Expired promotions fetched successfully", promotions);
    }
    
    /**
     * Get promotions starting soon
     */
    @GetMapping(ApiConstant.GET_PROMOTIONS_STARTING_SOON_API)
    @SwaggerOperation(summary = "Get promotions starting soon", description = "Retrieve a paginated list of promotions that are starting soon")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_READ)
    public ResponseEntity<ApiResponse<PaginatedResponse<PromotionInfoDto>>> getPromotionsStartingSoon(
        @ModelAttribute PromotionFilterParam promotionFilterParam) {
        log.info("Fetching promotions starting soon");
        
        Page<PromotionInfoDto> promotions = promotionManagementService.getPromotionsStartingSoon(
            PromotionFilterParam.standardize(promotionFilterParam));
        
        return ResponseBuilder.paginated("Promotions starting soon fetched successfully", promotions);
    }
    
    /**
     * Get promotions ending soon
     */
    @GetMapping(ApiConstant.GET_PROMOTIONS_ENDING_SOON_API)
    @SwaggerOperation(summary = "Get promotions ending soon", description = "Retrieve a paginated list of promotions that are ending soon")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_READ)
    public ResponseEntity<ApiResponse<PaginatedResponse<PromotionInfoDto>>> getPromotionsEndingSoon(
        @ModelAttribute PromotionFilterParam promotionFilterParam) {
        log.info("Fetching promotions ending soon");
        
        Page<PromotionInfoDto> promotions = promotionManagementService.getPromotionsEndingSoon(
            PromotionFilterParam.standardize(promotionFilterParam));
        
        return ResponseBuilder.paginated("Promotions ending soon fetched successfully", promotions);
    }
    
    /**
     * Get promotion usage history
     */
    @GetMapping(ApiConstant.GET_PROMOTION_USAGE_HISTORY_API)
    @SwaggerOperation(summary = "Get promotion usage history", description = "Retrieve a paginated list of promotion usage history with filters")
    // @RequirePermission(permissions = PermissionConstant.PROMOTION_READ)
    public ResponseEntity<ApiResponse<PaginatedResponse<PromotionUsageHistoryDto>>> getPromotionUsageHistory(
        @ModelAttribute PromotionUsageHistoryFilterParam filterParam) {
        log.info("Fetching promotion usage history");
        
        Page<PromotionUsageHistoryDto> usageHistory = promotionManagementService
            .getPromotionUsageHistory(PromotionUsageHistoryFilterParam.standardize(filterParam));
        
        return ResponseBuilder.paginated("Promotion usage history fetched successfully", usageHistory);
    }
}
