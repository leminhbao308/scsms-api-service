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
    private BigDecimal basePrice; // Total price = product costs + labor costs
    
    @Column(name = "labor_cost", precision = 15, scale = 2)
    private BigDecimal laborCost; // Tiền công lao động
    
    @Column(name = "product_cost", precision = 15, scale = 2)
    private BigDecimal productCost; // Tổng giá các sản phẩm (tự động tính từ ServiceProduct)
    
    @Column(name = "service_type")
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;
    
    @Column(name = "photo_required")
    @Builder.Default
    private Boolean photoRequired = false;
    
    
    
    @Column(name = "image_urls")
    @JdbcTypeCode(SqlTypes.JSON)
    private String imageUrls; // JSON array of image URLs
    
    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;
    
    // One-to-many relationship with service products
    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ServiceProduct> serviceProducts = new java.util.ArrayList<>();
    
    // Enums
    public enum SkillLevel {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }
    
    public enum ServiceType {
        MAINTENANCE, REPAIR, COSMETIC, INSPECTION, CUSTOM
    }
    
    // Business methods
    /**
     * Tính tổng giá sản phẩm từ danh sách ServiceProduct
     */
    public BigDecimal calculateProductCost() {
        if (serviceProducts == null || serviceProducts.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return serviceProducts.stream()
                .map(ServiceProduct::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Tính tổng giá service (sản phẩm + tiền công)
     */
    public BigDecimal calculateTotalPrice() {
        BigDecimal productCost = calculateProductCost();
        BigDecimal labor = laborCost != null ? laborCost : BigDecimal.ZERO;
        return productCost.add(labor);
    }
    
    /**
     * Cập nhật giá sản phẩm và tổng giá
     */
    public void updatePricing() {
        this.productCost = calculateProductCost();
        this.basePrice = calculateTotalPrice();
    }
}
