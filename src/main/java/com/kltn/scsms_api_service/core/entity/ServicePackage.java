package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "service_packages", schema = GeneralConstant.DB_SCHEMA_DEV)
public class ServicePackage extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "package_id", nullable = false)
    private UUID packageId;
    
    @Column(name = "package_url", unique = true, nullable = false, length = Integer.MAX_VALUE)
    private String packageUrl;
    
    @Column(name = "package_name", nullable = false, length = Integer.MAX_VALUE)
    private String packageName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;
    
    @Column(name = "total_duration")
    private Integer totalDuration; // in minutes
    
    @Column(name = "package_price", precision = 15, scale = 2)
    private BigDecimal packagePrice;
    
    @Column(name = "original_price", precision = 15, scale = 2)
    private BigDecimal originalPrice; // Total price if services were booked separately
    
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;
    
    @Column(name = "savings_amount", precision = 15, scale = 2)
    private BigDecimal savingsAmount;
    
    @Column(name = "package_type")
    @Enumerated(EnumType.STRING)
    private PackageType packageType;
    
    @Column(name = "target_vehicle_types")
    @JdbcTypeCode(SqlTypes.JSON)
    private String targetVehicleTypes; // JSON array of supported vehicle types
    
    @Column(name = "validity_period_days")
    private Integer validityPeriodDays; // How long the package is valid after purchase
    
    @Column(name = "max_usage_count")
    private Integer maxUsageCount; // Maximum number of times the package can be used
    
    @Column(name = "is_limited_time")
    @Builder.Default
    private Boolean isLimitedTime = false;
    
    @Column(name = "start_date")
    private java.time.LocalDate startDate;
    
    @Column(name = "end_date")
    private java.time.LocalDate endDate;
    
    @Column(name = "is_popular")
    @Builder.Default
    private Boolean isPopular = false;
    
    @Column(name = "is_recommended")
    @Builder.Default
    private Boolean isRecommended = false;
    
    @Column(name = "image_urls")
    @JdbcTypeCode(SqlTypes.JSON)
    private String imageUrls;
    
    @Column(name = "tags")
    @JdbcTypeCode(SqlTypes.JSON)
    private String tags;
    
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
    
    @Column(name = "terms_and_conditions", length = Integer.MAX_VALUE)
    private String termsAndConditions;
    
    // One-to-many relationship with service package steps
    @OneToMany(mappedBy = "servicePackage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.kltn.scsms_api_service.core.entity.ServicePackageStep> packageSteps = new java.util.ArrayList<>();
    
    // Enums
    public enum PackageType {
        MAINTENANCE, REPAIR, COSMETIC, INSPECTION, CUSTOM, SEASONAL, PROMOTIONAL
    }
}
