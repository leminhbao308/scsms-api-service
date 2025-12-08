package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaRepository extends JpaRepository<Media, UUID>, JpaSpecificationExecutor<Media> {
    
    /**
     * Find media by entity type and entity ID
     */
    List<Media> findByEntityTypeAndEntityId(Media.EntityType entityType, UUID entityId);
    
    /**
     * Find media by entity type and entity ID with pagination
     */
    Page<Media> findByEntityTypeAndEntityId(Media.EntityType entityType, UUID entityId, Pageable pageable);
    
    /**
     * Find media by entity type and entity ID, excluding deleted
     */
    List<Media> findByEntityTypeAndEntityIdAndIsDeletedFalse(Media.EntityType entityType, UUID entityId);
    
    /**
     * Find media by entity type and entity ID, excluding deleted, ordered by sort order
     */
    List<Media> findByEntityTypeAndEntityIdAndIsDeletedFalseOrderBySortOrderAsc(Media.EntityType entityType, UUID entityId);
    
    /**
     * Find main media for an entity
     */
    Optional<Media> findByEntityTypeAndEntityIdAndIsMainTrueAndIsDeletedFalse(Media.EntityType entityType, UUID entityId);
    
    /**
     * Find media by media type
     */
    List<Media> findByMediaType(Media.MediaType mediaType);
    
    /**
     * Find media by media type with pagination
     */
    Page<Media> findByMediaType(Media.MediaType mediaType, Pageable pageable);
    
    /**
     * Find media by entity type
     */
    List<Media> findByEntityType(Media.EntityType entityType);
    
    /**
     * Find media by entity type with pagination
     */
    Page<Media> findByEntityType(Media.EntityType entityType, Pageable pageable);
    
    /**
     * Find main media by entity type
     */
    List<Media> findByEntityTypeAndIsMainTrueAndIsDeletedFalse(Media.EntityType entityType);
    
    /**
     * Count media by entity type and entity ID
     */
    long countByEntityTypeAndEntityId(Media.EntityType entityType, UUID entityId);
    
    /**
     * Count media by entity type and entity ID, excluding deleted
     */
    long countByEntityTypeAndEntityIdAndIsDeletedFalse(Media.EntityType entityType, UUID entityId);
    
    /**
     * Count main media by entity type and entity ID
     */
    long countByEntityTypeAndEntityIdAndIsMainTrueAndIsDeletedFalse(Media.EntityType entityType, UUID entityId);
    
    /**
     * Get maximum sort order for an entity
     */
    @Query("SELECT COALESCE(MAX(m.sortOrder), 0) FROM Media m WHERE m.entityType = :entityType AND m.entityId = :entityId AND m.isDeleted = false")
    Integer getMaxSortOrderByEntity(@Param("entityType") Media.EntityType entityType, @Param("entityId") UUID entityId);
    
    /**
     * Update sort order for a specific media
     */
    @Modifying
    @Query("UPDATE Media m SET m.sortOrder = :sortOrder WHERE m.mediaId = :mediaId")
    void updateSortOrder(@Param("mediaId") UUID mediaId, @Param("sortOrder") Integer sortOrder);
    
    /**
     * Set all media for an entity as not main
     */
    @Modifying
    @Query("UPDATE Media m SET m.isMain = false WHERE m.entityType = :entityType AND m.entityId = :entityId AND m.isDeleted = false")
    void setAllAsNotMain(@Param("entityType") Media.EntityType entityType, @Param("entityId") UUID entityId);
    
    /**
     * Set a specific media as main (and others as not main)
     */
    @Modifying
    @Query("UPDATE Media m SET m.isMain = CASE WHEN m.mediaId = :mediaId THEN true ELSE false END WHERE m.entityType = :entityType AND m.entityId = :entityId AND m.isDeleted = false")
    void setAsMain(@Param("entityType") Media.EntityType entityType, @Param("entityId") UUID entityId, @Param("mediaId") UUID mediaId);
    
    /**
     * Find media by URL
     */
    Optional<Media> findByMediaUrl(String mediaUrl);
    
    /**
     * Check if media URL exists
     */
    boolean existsByMediaUrl(String mediaUrl);
    
    /**
     * Find media by URL, excluding specific media ID
     */
    boolean existsByMediaUrlAndMediaIdNot(String mediaUrl, UUID mediaId);
    
    /**
     * Get total media count
     */
    @Query("SELECT COUNT(m) FROM Media m")
    long getTotalMediaCount();
    
    /**
     * Get active media count
     */
    @Query("SELECT COUNT(m) FROM Media m WHERE m.isDeleted = false")
    long getActiveMediaCount();
    
    /**
     * Get media count by entity type
     */
    @Query("SELECT COUNT(m) FROM Media m WHERE m.entityType = :entityType AND m.isDeleted = false")
    long getMediaCountByEntityType(@Param("entityType") Media.EntityType entityType);
    
    /**
     * Get media count by media type
     */
    @Query("SELECT COUNT(m) FROM Media m WHERE m.mediaType = :mediaType AND m.isDeleted = false")
    long getMediaCountByMediaType(@Param("mediaType") Media.MediaType mediaType);
}
