package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

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
    
    @Column(name = "service_package_type_id")
    private UUID servicePackageTypeId;
    
    // One-to-many relationship with service package services
    @OneToMany(mappedBy = "servicePackage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ServicePackageService> packageServices = new java.util.ArrayList<>();
    
    // Quan hệ với ServiceProcess
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_process_id")
    private ServiceProcess serviceProcess;
    
    @Column(name = "is_default_process", nullable = false)
    @Builder.Default
    private Boolean isDefaultProcess = false; // Có sử dụng quy trình mặc định hay không
    
    @Column(name = "branch_id")
    private UUID branchId; // Chi nhánh cung cấp gói dịch vụ (nullable)
    
    // Business methods
    
    /**
     * Tính tổng giá từ các service trong package
     */
    public BigDecimal calculateServiceCost() {
        if (packageServices == null || packageServices.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return packageServices.stream()
                .filter(service -> service.getTotalPrice() != null)
                .map(ServicePackageService::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Tính tổng giá của package
     * Hỗ trợ 2 trường hợp:
     * 1. Combo Services: Tính từ các service con
     * 2. Service Process Only: Tính từ quy trình chăm sóc (KHÔNG có services)
     */
    public BigDecimal calculateTotalPrice() {
        // Trường hợp 1: Combo Services
        if (packageServices != null && !packageServices.isEmpty()) {
            return calculateServiceCost();
        }
        
        // Trường hợp 2: Service Process Only - có serviceProcess và KHÔNG có packageServices
        if (serviceProcess != null && (packageServices == null || packageServices.isEmpty())) {
            // Logic tính giá từ process sẽ được thực hiện bởi ServicePackagePricingCalculator
            // Entity chỉ trả về packagePrice đã được tính toán trước đó
            return packagePrice != null ? packagePrice : BigDecimal.ZERO;
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Tính tổng thời gian từ các service trong package
     */
    public Integer calculateTotalDuration() {
        if (packageServices == null || packageServices.isEmpty()) {
            return 0;
        }
        return packageServices.stream()
                .filter(service -> service.getService() != null && service.getService().getStandardDuration() != null)
                .mapToInt(service -> service.getService().getStandardDuration() * service.getQuantity())
                .sum();
    }
    
    /**
     * Cập nhật giá và thời gian package
     */
    public void updatePricing() {
        this.packagePrice = calculateTotalPrice();
        this.totalDuration = calculateTotalDuration();
    }
    
    /**
     * Kiểm tra xem package có sử dụng quy trình mặc định không
     */
    public boolean usesDefaultProcess() {
        return isDefaultProcess != null && isDefaultProcess;
    }
    
    /**
     * Lấy tên quy trình được sử dụng
     */
    public String getProcessName() {
        return serviceProcess != null ? serviceProcess.getName() : "Quy trình mặc định";
    }
    
    /**
     * Lấy code quy trình được sử dụng
     */
    public String getProcessCode() {
        return serviceProcess != null ? serviceProcess.getCode() : "DEFAULT_PROCESS";
    }
    
    /**
     * Lấy số lượng service trong package
     */
    public int getServiceCount() {
        return packageServices != null ? packageServices.size() : 0;
    }
    
    // Enums
}
