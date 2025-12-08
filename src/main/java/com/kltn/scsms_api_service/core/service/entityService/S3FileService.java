package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.S3File;
import com.kltn.scsms_api_service.core.entity.enumAttribute.FileStatus;
import com.kltn.scsms_api_service.core.entity.enumAttribute.FileType;
import com.kltn.scsms_api_service.core.repository.S3FileRepository;
import com.kltn.scsms_api_service.core.utils.S3Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileService {
    
    private final S3FileRepository s3FileRepository;
    private final S3Utils s3Utils;
    
    /**
     * Upload file và lưu thông tin vào database
     */
    @Transactional
    public S3File uploadAndSave(MultipartFile file, String folder, UUID uploadedBy,
                                String entityType, UUID entityId) {
        try {
            // Upload file to S3
            String fileUrl = s3Utils.uploadFile(file, folder);
            String fileKey = s3Utils.extractKeyFromUrl(fileUrl);
            
            // Determine file type
            FileType fileType = determineFileType(file.getContentType());
            
            // Create S3File entity
            S3File s3File = S3File.builder()
                .fileKey(fileKey)
                .fileUrl(fileUrl)
                .originalName(file.getOriginalFilename())
                .fileType(fileType)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .folder(folder)
                .uploadedBy(uploadedBy)
                .entityType(entityType)
                .entityId(entityId)
                .status(FileStatus.ACTIVE)
                .isTemporary(false)
                .accessCount(0)
                .build();
            
            S3File savedFile = s3FileRepository.save(s3File);
            log.info("File saved to database with ID: {}", savedFile.getId());
            
            return savedFile;
            
        } catch (Exception e) {
            log.error("Error uploading and saving file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload and save file", e);
        }
    }
    
    /**
     * Upload temporary file (with expiration)
     */
    @Transactional
    public S3File uploadTemporaryFile(MultipartFile file, String folder, UUID uploadedBy,
                                      int expirationHours) {
        try {
            String fileUrl = s3Utils.uploadFile(file, folder);
            String fileKey = s3Utils.extractKeyFromUrl(fileUrl);
            
            FileType fileType = determineFileType(file.getContentType());
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(expirationHours);
            
            S3File s3File = S3File.builder()
                .fileKey(fileKey)
                .fileUrl(fileUrl)
                .originalName(file.getOriginalFilename())
                .fileType(fileType)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .folder(folder)
                .uploadedBy(uploadedBy)
                .status(FileStatus.ACTIVE)
                .isTemporary(true)
                .expiresAt(expiresAt)
                .accessCount(0)
                .build();
            
            return s3FileRepository.save(s3File);
            
        } catch (Exception e) {
            log.error("Error uploading temporary file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload temporary file", e);
        }
    }
    
    /**
     * Upload multiple files
     */
    @Transactional
    public List<S3File> uploadMultiple(List<MultipartFile> files, String folder, UUID uploadedBy,
                                       String entityType, UUID entityId) {
        return files.stream()
            .map(file -> uploadAndSave(file, folder, uploadedBy, entityType, entityId))
            .collect(Collectors.toList());
    }
    
    /**
     * Get file by ID
     */
    public Optional<S3File> findById(Long id) {
        return s3FileRepository.findById(id);
    }
    
    /**
     * Get file by file key
     */
    public Optional<S3File> findByFileKey(String fileKey) {
        return Optional.ofNullable(s3FileRepository.findByFileKey(fileKey));
    }
    
    /**
     * Download file content
     */
    public byte[] downloadFile(Long fileId) {
        S3File s3File = s3FileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("File not found with id: " + fileId));
        
        if (!s3File.isActive()) {
            throw new RuntimeException("File is not active or has expired");
        }
        
        // Increment access count
        s3File.incrementAccessCount();
        s3FileRepository.save(s3File);
        
        return s3Utils.downloadFile(s3File.getFileKey());
    }
    
    /**
     * Generate presigned URL for file access
     */
    @Transactional
    public String generatePresignedUrl(Long fileId, int expirationMinutes) {
        S3File s3File = s3FileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("File not found with id: " + fileId));
        
        if (!s3File.isActive()) {
            throw new RuntimeException("File is not active or has expired");
        }
        
        s3File.incrementAccessCount();
        s3FileRepository.save(s3File);
        
        return s3Utils.generatePresignedUrl(s3File.getFileKey(), expirationMinutes);
    }
    
    /**
     * Soft delete file
     */
    @Transactional
    public void softDelete(Long fileId) {
        S3File s3File = s3FileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("File not found with id: " + fileId));
        
        s3File.markAsDeleted();
        s3FileRepository.save(s3File);
        
        log.info("File soft deleted: {}", fileId);
    }
    
    /**
     * Hard delete file (remove from S3 and database)
     */
    @Transactional
    public void hardDelete(Long fileId) {
        S3File s3File = s3FileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("File not found with id: " + fileId));
        
        // Delete from S3
        boolean deleted = s3Utils.deleteFile(s3File.getFileKey());
        
        if (deleted) {
            // Delete from database
            s3FileRepository.delete(s3File);
            log.info("File hard deleted: {}", fileId);
        } else {
            throw new RuntimeException("Failed to delete file from S3");
        }
    }
    
    /**
     * Delete multiple files
     */
    @Transactional
    public void deleteMultiple(List<Long> fileIds) {
        for (Long fileId : fileIds) {
            try {
                softDelete(fileId);
            } catch (Exception e) {
                log.error("Error deleting file {}: {}", fileId, e.getMessage());
            }
        }
    }
    
    /**
     * Archive file
     */
    @Transactional
    public void archiveFile(Long fileId) {
        S3File s3File = s3FileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("File not found with id: " + fileId));
        
        s3File.setStatus(FileStatus.ARCHIVED);
        s3FileRepository.save(s3File);
        
        log.info("File archived: {}", fileId);
    }
    
    /**
     * Update file metadata
     */
    @Transactional
    public S3File updateMetadata(Long fileId, String description, String metadata) {
        S3File s3File = s3FileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("File not found with id: " + fileId));
        
        if (description != null) {
            s3File.setDescription(description);
        }
        if (metadata != null) {
            s3File.setMetadata(metadata);
        }
        
        return s3FileRepository.save(s3File);
    }
    
    /**
     * Copy file
     */
    @Transactional
    public S3File copyFile(Long sourceFileId, String destinationFolder) {
        S3File sourceFile = s3FileRepository.findById(sourceFileId)
            .orElseThrow(() -> new RuntimeException("Source file not found"));
        
        String destinationKey = destinationFolder + sourceFile.getOriginalName();
        String newUrl = s3Utils.copyFile(sourceFile.getFileKey(), destinationKey);
        String newKey = s3Utils.extractKeyFromUrl(newUrl);
        
        S3File copiedFile = S3File.builder()
            .fileKey(newKey)
            .fileUrl(newUrl)
            .originalName(sourceFile.getOriginalName())
            .fileType(sourceFile.getFileType())
            .contentType(sourceFile.getContentType())
            .fileSize(sourceFile.getFileSize())
            .folder(destinationFolder)
            .uploadedBy(sourceFile.getUploadedBy())
            .entityType(sourceFile.getEntityType())
            .entityId(sourceFile.getEntityId())
            .status(FileStatus.ACTIVE)
            .isTemporary(false)
            .accessCount(0)
            .build();
        
        return s3FileRepository.save(copiedFile);
    }
    
    /**
     * Move file
     */
    @Transactional
    public S3File moveFile(Long fileId, String destinationFolder) {
        S3File sourceFile = s3FileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("File not found"));
        
        String destinationKey = destinationFolder + sourceFile.getOriginalName();
        String newUrl = s3Utils.moveFile(sourceFile.getFileKey(), destinationKey);
        String newKey = s3Utils.extractKeyFromUrl(newUrl);
        
        sourceFile.setFileKey(newKey);
        sourceFile.setFileUrl(newUrl);
        sourceFile.setFolder(destinationFolder);
        
        return s3FileRepository.save(sourceFile);
    }
    
    /**
     * Check if file exists
     */
    public boolean fileExists(String fileKey) {
        return s3Utils.doesFileExist(fileKey);
    }
    
    /**
     * Get file size
     */
    public long getFileSize(String fileKey) {
        return s3Utils.getFileSize(fileKey);
    }
    
    /**
     * Clean up expired temporary files
     */
    @Transactional
    public int cleanupExpiredFiles() {
        // This method should be called by a scheduled job
        List<S3File> expiredFiles = s3FileRepository.findAll().stream()
            .filter(S3File::isExpired)
            .collect(Collectors.toList());
        
        int deletedCount = 0;
        for (S3File file : expiredFiles) {
            try {
                hardDelete(file.getId());
                deletedCount++;
            } catch (Exception e) {
                log.error("Error deleting expired file {}: {}", file.getId(), e.getMessage());
            }
        }
        
        log.info("Cleaned up {} expired files", deletedCount);
        return deletedCount;
    }
    
    /**
     * Determine file type from content type
     */
    private FileType determineFileType(String contentType) {
        if (contentType == null) {
            return FileType.OTHER;
        }
        
        if (contentType.startsWith("image/")) {
            return FileType.IMAGE;
        } else if (contentType.startsWith("video/")) {
            return FileType.VIDEO;
        } else if (contentType.startsWith("audio/")) {
            return FileType.AUDIO;
        } else if (contentType.contains("pdf") || contentType.contains("document") ||
            contentType.contains("sheet") || contentType.contains("msword") ||
            contentType.contains("excel")) {
            return FileType.DOCUMENT;
        }
        
        return FileType.OTHER;
    }
    
    /**
     * Get all files by entity
     */
    public List<S3File> findByEntity(String entityType, Long entityId) {
        return s3FileRepository.findAll().stream()
            .filter(f -> entityType.equals(f.getEntityType()) &&
                entityId.equals(f.getEntityId()) &&
                f.getStatus() == FileStatus.ACTIVE)
            .collect(Collectors.toList());
    }
    
    /**
     * Get files by uploader
     */
    public List<S3File> findByUploader(UUID uploadedBy) {
        return s3FileRepository.findAll().stream()
            .filter(f -> uploadedBy.equals(f.getUploadedBy()) &&
                f.getStatus() == FileStatus.ACTIVE)
            .collect(Collectors.toList());
    }
    
    /**
     * Get files by type
     */
    public List<S3File> findByFileType(FileType fileType) {
        return s3FileRepository.findAll().stream()
            .filter(f -> f.getFileType() == fileType &&
                f.getStatus() == FileStatus.ACTIVE)
            .collect(Collectors.toList());
    }
}
