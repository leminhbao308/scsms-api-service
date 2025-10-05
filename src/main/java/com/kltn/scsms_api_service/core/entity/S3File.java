package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import com.kltn.scsms_api_service.core.entity.enumAttribute.FileStatus;
import com.kltn.scsms_api_service.core.entity.enumAttribute.FileType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "s3_files", schema = GeneralConstant.DB_SCHEMA_DEV,
    indexes = {
    @Index(name = "idx_file_key", columnList = "fileKey"),
    @Index(name = "idx_file_type", columnList = "fileType"),
    @Index(name = "idx_uploaded_by", columnList = "uploadedBy"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class S3File extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Key của file trên S3 (đường dẫn đầy đủ)
     * VD: images/avatars/user_1234567890_abc123.jpg
     */
    @Column(name = "file_key", nullable = false, unique = true, length = 500)
    private String fileKey;
    
    /**
     * URL công khai của file
     */
    @Column(name = "file_url", nullable = false, length = 1000)
    private String fileUrl;
    
    /**
     * Tên file gốc
     */
    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;
    
    /**
     * Loại file: IMAGE, DOCUMENT, VIDEO
     */
    @Column(name = "file_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private FileType fileType;
    
    /**
     * Content type của file (MIME type)
     * VD: image/jpeg, application/pdf
     */
    @Column(name = "content_type", length = 100)
    private String contentType;
    
    /**
     * Kích thước file (bytes)
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    
    /**
     * Folder/thư mục chứa file
     * VD: images/avatars/, documents/contracts/
     */
    @Column(name = "folder", length = 500)
    private String folder;
    
    /**
     * Mô tả file
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * ID của người upload
     */
    @Column(name = "uploaded_by")
    private UUID uploadedBy;
    
    /**
     * Entity type mà file này thuộc về
     * VD: USER, VEHICLE, SERVICE, INVOICE
     */
    @Column(name = "entity_type", length = 50)
    private String entityType;
    
    /**
     * ID của entity mà file này thuộc về
     */
    @Column(name = "entity_id")
    private UUID entityId;
    
    /**
     * Trạng thái file: ACTIVE, DELETED, ARCHIVED
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FileStatus status = FileStatus.ACTIVE;
    
    /**
     * File có phải là file tạm thời không
     */
    @Column(name = "is_temporary")
    @Builder.Default
    private Boolean isTemporary = false;
    
    /**
     * Thời gian hết hạn (cho file tạm thời)
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    /**
     * Số lần file được truy cập/download
     */
    @Column(name = "access_count")
    @Builder.Default
    private Integer accessCount = 0;
    
    /**
     * Lần truy cập cuối cùng
     */
    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;
    
    /**
     * Metadata bổ sung (JSON format)
     * VD: {"width": 1920, "height": 1080, "duration": 120}
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    /**
     * Enum cho loại file
     */

    
    /**
     * Increment access count
     */
    public void incrementAccessCount() {
        this.accessCount++;
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    /**
     * Mark file as deleted (soft delete)
     */
    public void markAsDeleted() {
        this.status = FileStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }
    
    /**
     * Check if file is expired
     */
    public boolean isExpired() {
        return this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt);
    }
    
    /**
     * Check if file is active
     */
    public boolean isActive() {
        return this.status == FileStatus.ACTIVE && !isExpired();
    }
    
    /**
     * Get file extension
     */
    public String getFileExtension() {
        if (this.originalName == null || !this.originalName.contains(".")) {
            return "";
        }
        return this.originalName.substring(this.originalName.lastIndexOf("."));
    }
    
    /**
     * Get human-readable file size
     */
    public String getFormattedFileSize() {
        if (this.fileSize == null) {
            return "0 B";
        }
        
        long bytes = this.fileSize;
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
