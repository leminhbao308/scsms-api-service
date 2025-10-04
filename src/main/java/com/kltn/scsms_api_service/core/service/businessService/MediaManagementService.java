package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.mediaManagement.MediaInfoDto;
import com.kltn.scsms_api_service.core.dto.mediaManagement.param.MediaFilterParam;
import com.kltn.scsms_api_service.core.dto.mediaManagement.request.*;
import com.kltn.scsms_api_service.core.entity.Media;
import com.kltn.scsms_api_service.core.service.entityService.MediaService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.MediaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaManagementService {
    
    private final MediaService mediaService;
    private final MediaMapper mediaMapper;
    
    /**
     * Get all media with pagination and filtering
     */
    public Page<MediaInfoDto> getAllMedia(MediaFilterParam filterParam) {
        log.debug("Fetching media with filter: {}", filterParam);
        return mediaService.getAllMedia(filterParam);
    }
    
    /**
     * Get media by ID
     */
    public MediaInfoDto getMediaById(UUID mediaId) {
        log.debug("Fetching media by ID: {}", mediaId);
        return mediaService.getMediaById(mediaId);
    }
    
    /**
     * Get media by entity type and entity ID
     */
    public List<MediaInfoDto> getMediaByEntity(String entityType, UUID entityId) {
        log.debug("Fetching media by entity: {} - {}", entityType, entityId);
        
        Media.EntityType entityTypeEnum = parseEntityType(entityType);
        return mediaService.getMediaByEntity(entityTypeEnum, entityId);
    }
    
    /**
     * Get main media for an entity
     */
    public MediaInfoDto getMainMediaByEntity(String entityType, UUID entityId) {
        log.debug("Fetching main media by entity: {} - {}", entityType, entityId);
        
        Media.EntityType entityTypeEnum = parseEntityType(entityType);
        return mediaService.getMainMediaByEntity(entityTypeEnum, entityId);
    }
    
    /**
     * Get media by media type
     */
    public List<MediaInfoDto> getMediaByType(String mediaType) {
        log.debug("Fetching media by type: {}", mediaType);
        
        Media.MediaType mediaTypeEnum = parseMediaType(mediaType);
        return mediaService.getMediaByType(mediaTypeEnum);
    }
    
    /**
     * Create new media
     */
    @Transactional
    public MediaInfoDto createMedia(CreateMediaRequest createRequest) {
        log.info("Creating media: {}", createRequest.getMediaUrl());
        
        // Validate request
        validateMediaCreateRequest(createRequest);
        
        // Media URL uniqueness check removed - allow duplicate URLs
        
        // Create media entity
        Media media = mediaMapper.toEntity(createRequest);
        
        return mediaService.createMedia(media);
    }
    
    /**
     * Update existing media
     */
    @Transactional
    public MediaInfoDto updateMedia(UUID mediaId, UpdateMediaRequest updateRequest) {
        log.info("Updating media: {}", mediaId);
        
        // Validate request
        validateMediaUpdateRequest(updateRequest);
        
        // Find existing media
        Media existingMedia = mediaService.findById(mediaId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, 
                "Media not found with ID: " + mediaId));
        
        // Media URL uniqueness check removed - allow duplicate URLs
        
        // Update media
        mediaMapper.updateEntityFromRequest(updateRequest, existingMedia);
        
        return mediaService.updateMedia(existingMedia);
    }
    
    /**
     * Delete media (soft delete)
     */
    @Transactional
    public void deleteMedia(UUID mediaId) {
        log.info("Soft deleting media: {}", mediaId);
        
        // Check if media exists
        if (!mediaService.findById(mediaId).isPresent()) {
            throw new ClientSideException(ErrorCode.NOT_FOUND, 
                "Media not found with ID: " + mediaId);
        }
        
        mediaService.deleteMedia(mediaId);
    }
    
    /**
     * Update media main status
     */
    @Transactional
    public MediaInfoDto updateMediaMainStatus(UUID mediaId, UpdateMediaMainStatusRequest statusRequest) {
        log.info("Updating media main status: {} to {}", mediaId, statusRequest.getIsMain());
        
        // Check if media exists
        if (!mediaService.findById(mediaId).isPresent()) {
            throw new ClientSideException(ErrorCode.NOT_FOUND, 
                "Media not found with ID: " + mediaId);
        }
        
        return mediaService.updateMediaMainStatus(mediaId, statusRequest.getIsMain());
    }
    
    /**
     * Bulk update media sort orders
     */
    @Transactional
    public void bulkUpdateMediaSortOrders(BulkUpdateMediaOrderRequest bulkRequest) {
        log.info("Bulk updating media sort orders for entity: {} - {}", 
            bulkRequest.getEntityType(), bulkRequest.getEntityId());
        
        // Validate request
        validateBulkUpdateRequest(bulkRequest);
        
        Media.EntityType entityType = parseEntityType(bulkRequest.getEntityType());
        UUID entityId = UUID.fromString(bulkRequest.getEntityId());
        
        // Extract media IDs and sort orders
        List<UUID> mediaIds = bulkRequest.getMediaOrders().stream()
            .map(order -> UUID.fromString(order.getMediaId()))
            .collect(Collectors.toList());
        
        List<Integer> sortOrders = bulkRequest.getMediaOrders().stream()
            .map(BulkUpdateMediaOrderRequest.MediaOrderDto::getSortOrder)
            .collect(Collectors.toList());
        
        mediaService.bulkUpdateMediaSortOrders(entityType, entityId, mediaIds, sortOrders);
    }
    
    /**
     * Validate media URL
     */
    public boolean validateMediaUrl(String mediaUrl) {
        if (mediaUrl == null || mediaUrl.trim().isEmpty()) {
            return false;
        }
        return mediaService.isMediaUrlUnique(mediaUrl);
    }
    
    /**
     * Get media statistics
     */
    public MediaStatsDto getMediaStatistics() {
        log.debug("Fetching media statistics");
        
        long totalMedia = mediaService.getTotalMediaCount();
        long activeMedia = mediaService.getActiveMediaCount();
        
        return MediaStatsDto.builder()
            .totalMedia(totalMedia)
            .activeMedia(activeMedia)
            .deletedMedia(totalMedia - activeMedia)
            .imageCount(mediaService.getMediaCountByMediaType(Media.MediaType.IMAGE))
            .videoCount(mediaService.getMediaCountByMediaType(Media.MediaType.VIDEO))
            .fileCount(mediaService.getMediaCountByMediaType(Media.MediaType.FILE))
            .build();
    }
    
    // ===== PRIVATE VALIDATION METHODS =====
    
    private void validateMediaCreateRequest(CreateMediaRequest request) {
        if (request == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Request cannot be null");
        }
        
        if (request.getEntityType() == null || request.getEntityType().trim().isEmpty()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Entity type is required");
        }
        
        if (request.getEntityId() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Entity ID is required");
        }
        
        if (request.getMediaUrl() == null || request.getMediaUrl().trim().isEmpty()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Media URL is required");
        }
        
        // Validate entity type
        try {
            Media.EntityType.valueOf(request.getEntityType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                "Invalid entity type: " + request.getEntityType());
        }
        
        // Validate media type if provided
        if (request.getMediaType() != null && !request.getMediaType().trim().isEmpty()) {
            try {
                Media.MediaType.valueOf(request.getMediaType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Invalid media type: " + request.getMediaType());
            }
        }
    }
    
    private void validateMediaUpdateRequest(UpdateMediaRequest request) {
        if (request == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Request cannot be null");
        }
        
        // At least one field should be provided for update
        if (request.getMediaUrl() == null && 
            request.getMediaType() == null && 
            request.getSortOrder() == null && 
            request.getAltText() == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "At least one field must be provided for update");
        }
        
        // Validate media type if provided
        if (request.getMediaType() != null && !request.getMediaType().trim().isEmpty()) {
            try {
                Media.MediaType.valueOf(request.getMediaType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Invalid media type: " + request.getMediaType());
            }
        }
    }
    
    private void validateBulkUpdateRequest(BulkUpdateMediaOrderRequest request) {
        if (request == null) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Request cannot be null");
        }
        
        if (request.getEntityType() == null || request.getEntityType().trim().isEmpty()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Entity type is required");
        }
        
        if (request.getEntityId() == null || request.getEntityId().trim().isEmpty()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Entity ID is required");
        }
        
        if (request.getMediaOrders() == null || request.getMediaOrders().isEmpty()) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Media orders list cannot be empty");
        }
        
        // Validate entity type
        try {
            Media.EntityType.valueOf(request.getEntityType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                "Invalid entity type: " + request.getEntityType());
        }
        
        // Validate entity ID format
        try {
            UUID.fromString(request.getEntityId());
        } catch (IllegalArgumentException e) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                "Invalid entity ID format: " + request.getEntityId());
        }
        
        // Validate media orders
        for (BulkUpdateMediaOrderRequest.MediaOrderDto order : request.getMediaOrders()) {
            if (order.getMediaId() == null || order.getMediaId().trim().isEmpty()) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, "Media ID is required in media orders");
            }
            
            if (order.getSortOrder() == null) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, "Sort order is required in media orders");
            }
            
            try {
                UUID.fromString(order.getMediaId());
            } catch (IllegalArgumentException e) {
                throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                    "Invalid media ID format: " + order.getMediaId());
            }
        }
    }
    
    private Media.EntityType parseEntityType(String entityType) {
        try {
            return Media.EntityType.valueOf(entityType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                "Invalid entity type: " + entityType);
        }
    }
    
    private Media.MediaType parseMediaType(String mediaType) {
        try {
            return Media.MediaType.valueOf(mediaType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, 
                "Invalid media type: " + mediaType);
        }
    }
    
    // ===== STATISTICS DTO =====
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MediaStatsDto {
        private long totalMedia;
        private long activeMedia;
        private long deletedMedia;
        private long imageCount;
        private long videoCount;
        private long fileCount;
    }
}
