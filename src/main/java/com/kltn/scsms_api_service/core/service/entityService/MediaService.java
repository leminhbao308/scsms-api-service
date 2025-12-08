package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.dto.mediaManagement.MediaInfoDto;
import com.kltn.scsms_api_service.core.dto.mediaManagement.param.MediaFilterParam;
import com.kltn.scsms_api_service.core.entity.Media;
import com.kltn.scsms_api_service.core.repository.MediaRepository;
import com.kltn.scsms_api_service.mapper.MediaMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaService {
    
    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;
    private final EntityManager entityManager;
    
    /**
     * Get all media with pagination and filtering
     */
    public Page<MediaInfoDto> getAllMedia(MediaFilterParam filterParam) {
        log.debug("Fetching media with filter: {}", filterParam);
        
        // Create criteria query
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Media> query = cb.createQuery(Media.class);
        Root<Media> root = query.from(Media.class);
        
        // Build predicates
        List<Predicate> predicates = new ArrayList<>();
        
        // Filter by entity type
        if (filterParam.getEntityType() != null && !filterParam.getEntityType().trim().isEmpty()) {
            try {
                Media.EntityType entityType = Media.EntityType.valueOf(filterParam.getEntityType().toUpperCase());
                predicates.add(cb.equal(root.get("entityType"), entityType));
            } catch (IllegalArgumentException e) {
                // Invalid entity type, return empty result
                return new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);
            }
        }
        
        // Filter by entity ID
        if (filterParam.getEntityId() != null) {
            predicates.add(cb.equal(root.get("entityId"), filterParam.getEntityId()));
        }
        
        // Filter by media type
        if (filterParam.getMediaType() != null && !filterParam.getMediaType().trim().isEmpty()) {
            try {
                Media.MediaType mediaType = Media.MediaType.valueOf(filterParam.getMediaType().toUpperCase());
                predicates.add(cb.equal(root.get("mediaType"), mediaType));
            } catch (IllegalArgumentException e) {
                // Invalid media type, return empty result
                return new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);
            }
        }
        
        // Filter by is main
        if (filterParam.getIsMain() != null) {
            predicates.add(cb.equal(root.get("isMain"), filterParam.getIsMain()));
        }
        
        // Filter by is deleted
        if (filterParam.getIsDeleted() != null) {
            predicates.add(cb.equal(root.get("isDeleted"), filterParam.getIsDeleted()));
        } else {
            // Default: exclude deleted records
            predicates.add(cb.equal(root.get("isDeleted"), false));
        }
        
        // Apply predicates
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        // Apply sorting
        Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");
        if (filterParam.getSort() != null && !filterParam.getSort().trim().isEmpty()) {
            Sort.Direction direction = filterParam.getDirection() != null && 
                filterParam.getDirection().equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(direction, filterParam.getSort());
        }
        
        // Apply pagination
        PageRequest pageRequest = PageRequest.of(
            filterParam.getPage(),
            filterParam.getSize(),
            sort
        );
        
        // Execute count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Media> countRoot = countQuery.from(Media.class);
        countQuery.select(cb.count(countRoot));
        
        List<Predicate> countPredicates = new ArrayList<>();
        if (filterParam.getEntityType() != null && !filterParam.getEntityType().trim().isEmpty()) {
            try {
                Media.EntityType entityType = Media.EntityType.valueOf(filterParam.getEntityType().toUpperCase());
                countPredicates.add(cb.equal(countRoot.get("entityType"), entityType));
            } catch (IllegalArgumentException e) {
                return new PageImpl<>(new ArrayList<>(), pageRequest, 0);
            }
        }
        if (filterParam.getEntityId() != null) {
            countPredicates.add(cb.equal(countRoot.get("entityId"), filterParam.getEntityId()));
        }
        if (filterParam.getMediaType() != null && !filterParam.getMediaType().trim().isEmpty()) {
            try {
                Media.MediaType mediaType = Media.MediaType.valueOf(filterParam.getMediaType().toUpperCase());
                countPredicates.add(cb.equal(countRoot.get("mediaType"), mediaType));
            } catch (IllegalArgumentException e) {
                return new PageImpl<>(new ArrayList<>(), pageRequest, 0);
            }
        }
        if (filterParam.getIsMain() != null) {
            countPredicates.add(cb.equal(countRoot.get("isMain"), filterParam.getIsMain()));
        }
        if (filterParam.getIsDeleted() != null) {
            countPredicates.add(cb.equal(countRoot.get("isDeleted"), filterParam.getIsDeleted()));
        } else {
            countPredicates.add(cb.equal(countRoot.get("isDeleted"), false));
        }
        
        if (!countPredicates.isEmpty()) {
            countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        }
        
        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();
        
        // Execute main query with pagination
        TypedQuery<Media> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageRequest.getOffset());
        typedQuery.setMaxResults(pageRequest.getPageSize());
        
        List<Media> mediaList = typedQuery.getResultList();
        List<MediaInfoDto> mediaDtos = mediaMapper.toInfoDtoList(mediaList);
        
        return new PageImpl<>(mediaDtos, pageRequest, totalElements);
    }
    
    /**
     * Get media by ID
     */
    public MediaInfoDto getMediaById(UUID mediaId) {
        log.debug("Fetching media by ID: {}", mediaId);
        
        Media media = mediaRepository.findById(mediaId)
            .orElseThrow(() -> new EntityNotFoundException("Media not found with ID: " + mediaId));
        
        if (media.getIsDeleted()) {
            throw new EntityNotFoundException("Media not found with ID: " + mediaId);
        }
        
        return mediaMapper.toInfoDto(media);
    }
    
    /**
     * Get media by entity type and entity ID
     */
    public List<MediaInfoDto> getMediaByEntity(Media.EntityType entityType, UUID entityId) {
        log.debug("Fetching media by entity: {} - {}", entityType, entityId);
        
        List<Media> mediaList = mediaRepository.findByEntityTypeAndEntityIdAndIsDeletedFalseOrderBySortOrderAsc(entityType, entityId);
        return mediaMapper.toInfoDtoList(mediaList);
    }
    
    /**
     * Get main media for an entity
     * If no main media is found, returns the first media (by sort order) as fallback
     * If no media exists at all, throws EntityNotFoundException
     */
    public MediaInfoDto getMainMediaByEntity(Media.EntityType entityType, UUID entityId) {
        log.debug("Fetching main media by entity: {} - {}", entityType, entityId);
        
        // First, try to find main media
        Optional<Media> mainMedia = mediaRepository.findByEntityTypeAndEntityIdAndIsMainTrueAndIsDeletedFalse(entityType, entityId);
        
        if (mainMedia.isPresent()) {
            return mediaMapper.toInfoDto(mainMedia.get());
        }
        
        // If no main media, fallback to first media by sort order
        log.debug("Main media not found for entity: {} - {}, falling back to first media", entityType, entityId);
        List<Media> mediaList = mediaRepository.findByEntityTypeAndEntityIdAndIsDeletedFalseOrderBySortOrderAsc(entityType, entityId);
        
        if (mediaList.isEmpty()) {
            throw new EntityNotFoundException("No media found for entity: " + entityType + " - " + entityId);
        }
        
        // Return first media as fallback
        return mediaMapper.toInfoDto(mediaList.get(0));
    }
    
    /**
     * Get media by media type
     */
    public List<MediaInfoDto> getMediaByType(Media.MediaType mediaType) {
        log.debug("Fetching media by type: {}", mediaType);
        
        List<Media> mediaList = mediaRepository.findByMediaType(mediaType);
        return mediaMapper.toInfoDtoList(mediaList);
    }
    
    /**
     * Create new media
     */
    @Transactional
    public MediaInfoDto createMedia(Media media) {
        log.debug("Creating media: {}", media.getMediaUrl());
        
        // Ensure audit fields are set
        if (media.getIsDeleted() == null) {
            media.setIsDeleted(false);
        }
        
        // If this is set as main, unset other main media for the same entity
        if (media.getIsMain() != null && media.getIsMain()) {
            mediaRepository.setAllAsNotMain(media.getEntityType(), media.getEntityId());
        }
        
        // If sort order is not set, set it to max + 1
        if (media.getSortOrder() == null) {
            Integer maxSortOrder = mediaRepository.getMaxSortOrderByEntity(media.getEntityType(), media.getEntityId());
            media.setSortOrder(maxSortOrder + 1);
        }
        
        Media savedMedia = mediaRepository.save(media);
        return mediaMapper.toInfoDto(savedMedia);
    }
    
    /**
     * Update existing media
     */
    @Transactional
    public MediaInfoDto updateMedia(Media media) {
        log.debug("Updating media: {}", media.getMediaId());
        
        // Ensure audit fields are set
        if (media.getIsDeleted() == null) {
            media.setIsDeleted(false);
        }
        
        Media savedMedia = mediaRepository.save(media);
        return mediaMapper.toInfoDto(savedMedia);
    }
    
    /**
     * Soft delete media
     */
    @Transactional
    public void deleteMedia(UUID mediaId) {
        log.debug("Soft deleting media: {}", mediaId);
        
        Media media = mediaRepository.findById(mediaId)
            .orElseThrow(() -> new EntityNotFoundException("Media not found with ID: " + mediaId));
        
        media.setIsDeleted(true);
        mediaRepository.save(media);
    }
    
    /**
     * Update media main status
     */
    @Transactional
    public MediaInfoDto updateMediaMainStatus(UUID mediaId, Boolean isMain) {
        log.debug("Updating media main status: {} to {}", mediaId, isMain);
        
        Media media = mediaRepository.findById(mediaId)
            .orElseThrow(() -> new EntityNotFoundException("Media not found with ID: " + mediaId));
        
        if (isMain) {
            // Set all other media for this entity as not main
            mediaRepository.setAllAsNotMain(media.getEntityType(), media.getEntityId());
        }
        
        media.setIsMain(isMain);
        Media savedMedia = mediaRepository.save(media);
        return mediaMapper.toInfoDto(savedMedia);
    }
    
    /**
     * Update media sort order
     */
    @Transactional
    public void updateMediaSortOrder(UUID mediaId, Integer sortOrder) {
        log.debug("Updating media sort order: {} to {}", mediaId, sortOrder);
        
        mediaRepository.updateSortOrder(mediaId, sortOrder);
    }
    
    /**
     * Bulk update media sort orders
     */
    @Transactional
    public void bulkUpdateMediaSortOrders(Media.EntityType entityType, UUID entityId, List<UUID> mediaIds, List<Integer> sortOrders) {
        log.debug("Bulk updating media sort orders for entity: {} - {}", entityType, entityId);
        
        if (mediaIds.size() != sortOrders.size()) {
            throw new IllegalArgumentException("Media IDs and sort orders lists must have the same size");
        }
        
        for (int i = 0; i < mediaIds.size(); i++) {
            mediaRepository.updateSortOrder(mediaIds.get(i), sortOrders.get(i));
        }
    }
    
    /**
     * Check if media URL exists
     */
    public boolean isMediaUrlUnique(String mediaUrl) {
        return !mediaRepository.existsByMediaUrl(mediaUrl);
    }
    
    /**
     * Check if media URL exists, excluding specific media ID
     */
    public boolean isMediaUrlUnique(String mediaUrl, UUID mediaId) {
        return !mediaRepository.existsByMediaUrlAndMediaIdNot(mediaUrl, mediaId);
    }
    
    /**
     * Find media entity by ID
     */
    public Optional<Media> findById(UUID mediaId) {
        return mediaRepository.findById(mediaId);
    }
    
    /**
     * Get media statistics
     */
    public long getTotalMediaCount() {
        return mediaRepository.getTotalMediaCount();
    }
    
    public long getActiveMediaCount() {
        return mediaRepository.getActiveMediaCount();
    }
    
    public long getMediaCountByEntityType(Media.EntityType entityType) {
        return mediaRepository.getMediaCountByEntityType(entityType);
    }
    
    public long getMediaCountByMediaType(Media.MediaType mediaType) {
        return mediaRepository.getMediaCountByMediaType(mediaType);
    }
}
