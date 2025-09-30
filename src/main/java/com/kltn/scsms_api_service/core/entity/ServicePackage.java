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
    private BigDecimal packagePrice; // Total price = sum of service prices + sum of product prices
    
    @Column(name = "package_type")
    @Enumerated(EnumType.STRING)
    private PackageType packageType;
    
    
    @Column(name = "image_urls")
    @JdbcTypeCode(SqlTypes.JSON)
    private String imageUrls;
    
    
    // One-to-many relationship with service package steps
    @OneToMany(mappedBy = "servicePackage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.kltn.scsms_api_service.core.entity.ServicePackageStep> packageSteps = new java.util.ArrayList<>();
    
    // One-to-many relationship with service package products
    @OneToMany(mappedBy = "servicePackage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ServicePackageProduct> packageProducts = new java.util.ArrayList<>();
    
    // One-to-many relationship with service package services
    @OneToMany(mappedBy = "servicePackage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ServicePackageService> packageServices = new java.util.ArrayList<>();
    
    // Business methods
    
    /**
     * Tính tổng giá từ các service trực tiếp trong package
     */
    public BigDecimal calculateServiceCostFromServices() {
        if (packageServices == null || packageServices.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return packageServices.stream()
                .filter(service -> service.getTotalPrice() != null)
                .map(ServicePackageService::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Tính tổng giá từ tất cả service trong package (direct services only)
     */
    public BigDecimal calculateServiceCost() {
        return calculateServiceCostFromServices();
    }
    
    /**
     * Tính tổng giá từ các sản phẩm trong package
     */
    public BigDecimal calculateProductCost() {
        if (packageProducts == null || packageProducts.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return packageProducts.stream()
                .filter(product -> product.getTotalPrice() != null)
                .map(ServicePackageProduct::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Tính tổng giá của package (service + product)
     */
    public BigDecimal calculateTotalPrice() {
        return calculateServiceCost().add(calculateProductCost());
    }
    
    
    /**
     * Tính tổng thời gian từ các service trực tiếp trong package
     */
    public Integer calculateDurationFromServices() {
        if (packageServices == null || packageServices.isEmpty()) {
            return 0;
        }
        return packageServices.stream()
                .filter(service -> service.getService() != null && service.getService().getStandardDuration() != null)
                .mapToInt(service -> service.getService().getStandardDuration() * service.getQuantity())
                .sum();
    }
    
    /**
     * Tính tổng thời gian từ tất cả service trong package (direct services only)
     */
    public Integer calculateTotalDuration() {
        return calculateDurationFromServices();
    }
    
    /**
     * Cập nhật giá và thời gian package
     */
    public void updatePricing() {
        this.packagePrice = calculateTotalPrice();
        this.totalDuration = calculateTotalDuration();
    }
    
    // Enums
    public enum PackageType {
        MAINTENANCE, REPAIR, COSMETIC, INSPECTION, CUSTOM
    }
}
