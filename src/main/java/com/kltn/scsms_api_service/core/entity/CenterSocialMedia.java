package com.kltn.scsms_api_service.core.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Table(name = "center_social_media", schema = GeneralConstant.DB_SCHEMA_DEV)
public class CenterSocialMedia extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "social_media_id", nullable = false)
    private UUID socialMediaId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id", nullable = false)
    @JsonBackReference
    private Center center;
    
    @Column(name = "platform", nullable = false, length = 50)
    private String platform;
    
    @Column(name = "url", nullable = false, length = 255)
    private String url;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    // Utility methods
    public void setCenter(Center center) {
        this.center = center;
    }
    
    public void activate() {
        this.isActive = true;
    }
    
    public void deactivate() {
        this.isActive = false;
    }
}
