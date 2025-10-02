package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.mediaManagement.MediaInfoDto;
import com.kltn.scsms_api_service.core.dto.mediaManagement.param.MediaFilterParam;
import com.kltn.scsms_api_service.core.dto.mediaManagement.request.*;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.response.PaginatedResponse;
import com.kltn.scsms_api_service.core.service.businessService.MediaManagementService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Controller handling media management operations
 * Manages media files (images, videos, documents) for various entities
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Media Management", description = "Media management endpoints for file uploads and management")
public class MediaManagementController {

    private final MediaManagementService mediaManagementService;

    /**
     * Get all media with pagination and filtering
     */
    @GetMapping(ApiConstant.GET_ALL_MEDIA_API)
    @SwaggerOperation(summary = "Get all media", description = "Retrieve a paginated list of all media with filtering options")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<PaginatedResponse<MediaInfoDto>>> getAllMedia(
            @ModelAttribute MediaFilterParam mediaFilterParam) {
        log.info("Fetching all media with filters: {}", mediaFilterParam);

        Page<MediaInfoDto> media = mediaManagementService.getAllMedia(
                MediaFilterParam.standardize(mediaFilterParam));

        return ResponseBuilder.paginated("Media fetched successfully", media);
    }

    /**
     * Get media by ID
     */
    @GetMapping(ApiConstant.GET_MEDIA_BY_ID_API)
    @SwaggerOperation(summary = "Get media by ID", description = "Retrieve a specific media by its ID")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<MediaInfoDto>> getMediaById(
            @PathVariable("mediaId") String mediaId) {
        log.info("Fetching media by ID: {}", mediaId);

        MediaInfoDto media = mediaManagementService.getMediaById(UUID.fromString(mediaId));

        return ResponseBuilder.success("Media fetched successfully", media);
    }

    /**
     * Get media by entity type and entity ID
     */
    @GetMapping(ApiConstant.GET_MEDIA_BY_ENTITY_API)
    @SwaggerOperation(summary = "Get media by entity", description = "Retrieve all media for a specific entity")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<List<MediaInfoDto>>> getMediaByEntity(
            @PathVariable("entityType") String entityType,
            @PathVariable("entityId") String entityId) {
        log.info("Fetching media by entity: {} - {}", entityType, entityId);

        List<MediaInfoDto> media = mediaManagementService.getMediaByEntity(entityType, UUID.fromString(entityId));

        return ResponseBuilder.success("Media fetched successfully", media);
    }

    /**
     * Get main media for an entity
     */
    @GetMapping(ApiConstant.GET_MAIN_MEDIA_BY_ENTITY_API)
    @SwaggerOperation(summary = "Get main media by entity", description = "Retrieve the main media for a specific entity")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<MediaInfoDto>> getMainMediaByEntity(
            @PathVariable("entityType") String entityType,
            @PathVariable("entityId") String entityId) {
        log.info("Fetching main media by entity: {} - {}", entityType, entityId);

        MediaInfoDto media = mediaManagementService.getMainMediaByEntity(entityType, UUID.fromString(entityId));

        return ResponseBuilder.success("Main media fetched successfully", media);
    }

    /**
     * Get media by media type
     */
    @GetMapping(ApiConstant.GET_MEDIA_BY_TYPE_API)
    @SwaggerOperation(summary = "Get media by type", description = "Retrieve all media of a specific type")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<List<MediaInfoDto>>> getMediaByType(
            @PathVariable("mediaType") String mediaType) {
        log.info("Fetching media by type: {}", mediaType);

        List<MediaInfoDto> media = mediaManagementService.getMediaByType(mediaType);

        return ResponseBuilder.success("Media fetched successfully", media);
    }

    /**
     * Create new media
     */
    @PostMapping(ApiConstant.CREATE_MEDIA_API)
    @SwaggerOperation(summary = "Create new media", description = "Create a new media record for an entity")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<MediaInfoDto>> createMedia(
            @Valid @RequestBody CreateMediaRequest createRequest) {
        log.info("Creating new media for entity: {} - {}", 
                createRequest.getEntityType(), createRequest.getEntityId());

        MediaInfoDto createdMedia = mediaManagementService.createMedia(createRequest);

        return ResponseBuilder.created("Media created successfully", createdMedia);
    }

    /**
     * Update existing media
     */
    @PostMapping(ApiConstant.UPDATE_MEDIA_API)
    @SwaggerOperation(summary = "Update existing media", description = "Update media details including URL, type, and metadata")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<MediaInfoDto>> updateMedia(
            @PathVariable("mediaId") String mediaId,
            @Valid @RequestBody UpdateMediaRequest updateRequest) {
        log.info("Updating media with ID: {}", mediaId);

        MediaInfoDto updatedMedia = mediaManagementService.updateMedia(
                UUID.fromString(mediaId), updateRequest);

        return ResponseBuilder.success("Media updated successfully", updatedMedia);
    }

    /**
     * Delete media (soft delete)
     */
    @PostMapping(ApiConstant.DELETE_MEDIA_API)
    @SwaggerOperation(summary = "Delete media", description = "Soft delete a media record")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<Void>> deleteMedia(
            @PathVariable("mediaId") String mediaId) {
        log.info("Soft deleting media with ID: {}", mediaId);

        mediaManagementService.deleteMedia(UUID.fromString(mediaId));

        return ResponseBuilder.success("Media deleted successfully");
    }

    /**
     * Update media main status
     */
    @PostMapping(ApiConstant.UPDATE_MEDIA_MAIN_STATUS_API)
    @SwaggerOperation(summary = "Update media main status", description = "Update whether a media is the main media for its entity")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<MediaInfoDto>> updateMediaMainStatus(
            @PathVariable("mediaId") String mediaId,
            @Valid @RequestBody UpdateMediaMainStatusRequest statusRequest) {
        log.info("Updating media main status for ID: {} to {}", mediaId, statusRequest.getIsMain());

        MediaInfoDto updatedMedia = mediaManagementService.updateMediaMainStatus(
                UUID.fromString(mediaId), statusRequest);

        return ResponseBuilder.success("Media main status updated successfully", updatedMedia);
    }

    /**
     * Bulk update media sort orders
     */
    @PostMapping(ApiConstant.BULK_UPDATE_MEDIA_ORDER_API)
    @SwaggerOperation(summary = "Bulk update media order", description = "Update sort orders for multiple media items")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<Void>> bulkUpdateMediaOrder(
            @Valid @RequestBody BulkUpdateMediaOrderRequest bulkRequest) {
        log.info("Bulk updating media order for entity: {} - {}", 
                bulkRequest.getEntityType(), bulkRequest.getEntityId());

        mediaManagementService.bulkUpdateMediaSortOrders(bulkRequest);

        return ResponseBuilder.success("Media order updated successfully");
    }

    /**
     * Validate media URL
     */
    @GetMapping(ApiConstant.VALIDATE_MEDIA_URL_API)
    @SwaggerOperation(summary = "Validate media URL", description = "Check if a media URL is available for use")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<Boolean>> validateMediaUrl(
            @RequestParam("url") String mediaUrl) {
        log.info("Validating media URL: {}", mediaUrl);

        boolean isValid = mediaManagementService.validateMediaUrl(mediaUrl);

        return ResponseBuilder.success("Media URL validation completed", isValid);
    }

    /**
     * Get media statistics
     */
    @GetMapping(ApiConstant.GET_MEDIA_STATISTICS_API)
    @SwaggerOperation(summary = "Get media statistics", description = "Retrieve statistics about media files")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<MediaManagementService.MediaStatsDto>> getMediaStatistics() {
        log.info("Fetching media statistics");

        MediaManagementService.MediaStatsDto statistics = 
            mediaManagementService.getMediaStatistics();

        return ResponseBuilder.success("Media statistics fetched successfully", statistics);
    }
}
