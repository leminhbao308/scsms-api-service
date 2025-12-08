package com.kltn.scsms_api_service.abstracts;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base audit entity class that provides common audit fields for all entities.
 * This class contains creation and modification tracking fields.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@SuperBuilder
public abstract class AuditEntity {
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
    
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;
    
    @CreatedBy
    @Column(name = "created_by", length = 100, updatable = false)
    @Builder.Default
    private String createdBy = "SYSTEM";
    
    @LastModifiedBy
    @Column(name = "modified_by", length = 100)
    private String modifiedBy;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    // Constructors
    public AuditEntity() {
    }
    
    // Utility methods
    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
        if (modifiedDate == null) {
            modifiedDate = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }
    
    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
        
        // deactivate the entity when it is marked as deleted, but not vice versa
        if (isDeleted) {
            this.isActive = false;
        }
    }
}
