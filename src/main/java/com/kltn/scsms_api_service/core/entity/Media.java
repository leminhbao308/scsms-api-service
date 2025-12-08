package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "media", schema = GeneralConstant.DB_SCHEMA_DEV)
public class Media extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "media_id", nullable = false)
    private UUID mediaId;
    
    @Column(name = "entity_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private EntityType entityType;
    
    @Column(name = "entity_id", nullable = false)
    private UUID entityId;
    
    @Column(name = "media_url", nullable = false, length = Integer.MAX_VALUE)
    private String mediaUrl;
    
    @Column(name = "media_type", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MediaType mediaType = MediaType.IMAGE;
    
    @Column(name = "is_main")
    @Builder.Default
    private Boolean isMain = false;
    
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
    
    @Column(name = "alt_text", length = 255)
    private String altText;
    
    // Enums
    public enum EntityType {
        PRODUCT, SERVICE, PACKAGE, BRANCH, PROMOTION, CATEGORY, CENTER, USER
    }
    
    public enum MediaType {
        IMAGE, VIDEO, FILE, DOCUMENT, AUDIO
    }
    
    // Utility methods
    public void setAsMain() {
        this.isMain = true;
    }
    
    public void setAsNotMain() {
        this.isMain = false;
    }
    
    public boolean isImage() {
        return MediaType.IMAGE.equals(this.mediaType);
    }
    
    public boolean isVideo() {
        return MediaType.VIDEO.equals(this.mediaType);
    }
}
