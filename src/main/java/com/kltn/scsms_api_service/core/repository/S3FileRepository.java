package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.S3File;
import com.kltn.scsms_api_service.core.entity.enumAttribute.FileStatus;
import com.kltn.scsms_api_service.core.entity.enumAttribute.FileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface S3FileRepository extends JpaRepository<S3File, Long> {
    
    /**
     * Find file by file key
     */
    S3File findByFileKey(String fileKey);
    
    /**
     * Find all active files by entity
     */
    @Query("SELECT f FROM S3File f WHERE f.entityType = :entityType " +
        "AND f.entityId = :entityId AND f.status = 'ACTIVE'")
    List<S3File> findActiveByEntity(@Param("entityType") String entityType,
                                    @Param("entityId") Long entityId);
    
    /**
     * Find all files by uploader
     */
    @Query("SELECT f FROM S3File f WHERE f.uploadedBy = :uploadedBy " +
        "AND f.status = 'ACTIVE' ORDER BY f.createdDate DESC")
    List<S3File> findActiveByUploader(@Param("uploadedBy") UUID uploadedBy);
    
    /**
     * Find all files by file type
     */
    @Query("SELECT f FROM S3File f WHERE f.fileType = :fileType " +
        "AND f.status = 'ACTIVE' ORDER BY f.createdDate DESC")
    List<S3File> findActiveByFileType(@Param("fileType") FileType fileType);
    
    /**
     * Find all expired temporary files
     */
    @Query("SELECT f FROM S3File f WHERE f.isTemporary = true " +
        "AND f.expiresAt < :currentTime AND f.status = 'ACTIVE'")
    List<S3File> findExpiredTemporaryFiles(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find all files in folder
     */
    @Query("SELECT f FROM S3File f WHERE f.folder = :folder " +
        "AND f.status = 'ACTIVE' ORDER BY f.createdDate DESC")
    List<S3File> findActiveByFolder(@Param("folder") String folder);
    
    /**
     * Find files by status
     */
    @Query("SELECT f FROM S3File f WHERE f.status = :status ORDER BY f.createdDate DESC")
    List<S3File> findByStatus(@Param("status") FileStatus status);
    
    /**
     * Count files by entity
     */
    @Query("SELECT COUNT(f) FROM S3File f WHERE f.entityType = :entityType " +
        "AND f.entityId = :entityId AND f.status = 'ACTIVE'")
    long countByEntity(@Param("entityType") String entityType, @Param("entityId") Long entityId);
    
    /**
     * Count files by uploader
     */
    @Query("SELECT COUNT(f) FROM S3File f WHERE f.uploadedBy = :uploadedBy AND f.status = 'ACTIVE'")
    long countByUploader(@Param("uploadedBy") UUID uploadedBy);
    
    /**
     * Get total file size by uploader
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM S3File f " +
        "WHERE f.uploadedBy = :uploadedBy AND f.status = 'ACTIVE'")
    long getTotalFileSizeByUploader(@Param("uploadedBy") UUID uploadedBy);
    
    /**
     * Find files created between dates
     */
    @Query("SELECT f FROM S3File f WHERE f.createdDate BETWEEN :startDate AND :endDate " +
        "AND f.status = 'ACTIVE' ORDER BY f.createdDate DESC")
    List<S3File> findByCreatedDateBetween(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find large files (above size limit)
     */
    @Query("SELECT f FROM S3File f WHERE f.fileSize > :sizeLimit " +
        "AND f.status = 'ACTIVE' ORDER BY f.fileSize DESC")
    List<S3File> findLargeFiles(@Param("sizeLimit") long sizeLimit);
    
    /**
     * Find recently accessed files
     */
    @Query("SELECT f FROM S3File f WHERE f.lastAccessedAt IS NOT NULL " +
        "AND f.status = 'ACTIVE' ORDER BY f.lastAccessedAt DESC")
    List<S3File> findRecentlyAccessedFiles();
    
    /**
     * Find files with most accesses
     */
    @Query("SELECT f FROM S3File f WHERE f.status = 'ACTIVE' " +
        "ORDER BY f.accessCount DESC")
    List<S3File> findMostAccessedFiles();
    
    /**
     * Delete old temporary files
     */
    @Query("SELECT f FROM S3File f WHERE f.isTemporary = true " +
        "AND f.createdDate < :cutoffDate AND f.status = 'ACTIVE'")
    List<S3File> findOldTemporaryFiles(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find files by entity type only
     */
    @Query("SELECT f FROM S3File f WHERE f.entityType = :entityType " +
        "AND f.status = 'ACTIVE' ORDER BY f.createdDate DESC")
    List<S3File> findActiveByEntityType(@Param("entityType") String entityType);
    
    /**
     * Find files uploaded today by user
     */
    @Query("SELECT f FROM S3File f WHERE f.uploadedBy = :uploadedBy " +
        "AND CAST(f.createdDate AS DATE) = CURRENT_DATE AND f.status = 'ACTIVE'")
    List<S3File> findUploadedTodayByUser(@Param("uploadedBy") UUID uploadedBy);
    
    /**
     * Count files by file type for user
     */
    @Query("SELECT COUNT(f) FROM S3File f WHERE f.uploadedBy = :uploadedBy " +
        "AND f.fileType = :fileType AND f.status = 'ACTIVE'")
    long countByUploaderAndFileType(@Param("uploadedBy") UUID uploadedBy,
                                    @Param("fileType") FileType fileType);
    
    /**
     * Find duplicate files by original name
     */
    @Query("SELECT f FROM S3File f WHERE f.originalName = :originalName " +
        "AND f.status = 'ACTIVE' ORDER BY f.createdDate DESC")
    List<S3File> findByOriginalName(@Param("originalName") String originalName);
    
    /**
     * Find files by content type
     */
    @Query("SELECT f FROM S3File f WHERE f.contentType = :contentType " +
        "AND f.status = 'ACTIVE' ORDER BY f.createdDate DESC")
    List<S3File> findByContentType(@Param("contentType") String contentType);
}
