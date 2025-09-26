package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.core.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "services", schema = GeneralConstant.DB_SCHEMA_DEV)
public class Service extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "service_id", nullable = false)
    private UUID serviceId;
    
    @Column(name = "service_url", unique = true, nullable = false, length = Integer.MAX_VALUE)
    private String serviceUrl;
    
    @Column(name = "service_name", nullable = false, length = Integer.MAX_VALUE)
    private String serviceName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;
    
    @Column(name = "standard_duration")
    private Integer standardDuration; // in minutes
    
    @Column(name = "required_skill_level")
    @Enumerated(EnumType.STRING)
    private SkillLevel requiredSkillLevel;
    
    @Column(name = "is_package")
    @Builder.Default
    private Boolean isPackage = false;
    
    @Column(name = "base_price", precision = 15, scale = 2)
    private BigDecimal basePrice;
    
    @Column(name = "min_price", precision = 15, scale = 2)
    private BigDecimal minPrice;
    
    @Column(name = "max_price", precision = 15, scale = 2)
    private BigDecimal maxPrice;
    
    @Column(name = "complexity_level")
    @Enumerated(EnumType.STRING)
    private ComplexityLevel complexityLevel;
    
    @Column(name = "service_type")
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;
    
    @Column(name = "vehicle_types")
    @JdbcTypeCode(SqlTypes.JSON)
    private String vehicleTypes; // JSON array of supported vehicle types
    
    @Column(name = "required_tools")
    @JdbcTypeCode(SqlTypes.JSON)
    private String requiredTools; // JSON array of required tools
    
    @Column(name = "safety_notes", length = Integer.MAX_VALUE)
    private String safetyNotes;
    
    @Column(name = "quality_criteria", length = Integer.MAX_VALUE)
    private String qualityCriteria;
    
    @Column(name = "photo_required")
    @Builder.Default
    private Boolean photoRequired = false;
    
    @Column(name = "customer_approval_required")
    @Builder.Default
    private Boolean customerApprovalRequired = false;
    
    @Column(name = "is_express_service")
    @Builder.Default
    private Boolean isExpressService = false;
    
    @Column(name = "is_premium_service")
    @Builder.Default
    private Boolean isPremiumService = false;
    
    @Column(name = "image_urls")
    @JdbcTypeCode(SqlTypes.JSON)
    private String imageUrls;
    
    @Column(name = "tags")
    @JdbcTypeCode(SqlTypes.JSON)
    private String tags;
    
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
    
    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;
    
    // Enums
    public enum SkillLevel {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }
    
    public enum ComplexityLevel {
        BASIC, INTERMEDIATE, ADVANCED, EXPERT
    }
    
    public enum ServiceType {
        MAINTENANCE, REPAIR, COSMETIC, INSPECTION, CUSTOM
    }
}
