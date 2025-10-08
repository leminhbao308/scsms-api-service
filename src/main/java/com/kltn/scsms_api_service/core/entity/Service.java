package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

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

    @Column(name = "estimated_duration")
    private Integer estimatedDuration; // Tổng thời gian dự kiến từ service process (phút)

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

    @Column(name = "service_type_id")
    private UUID serviceTypeId;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    // Quan hệ với ServiceProcess
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_process_id")
    private ServiceProcess serviceProcess;

    @Column(name = "is_default_process", nullable = false)
    @Builder.Default
    private Boolean isDefaultProcess = false; // Có sử dụng quy trình mặc định hay không

    @Column(name = "branch_id")
    private UUID branchId; // Chi nhánh cung cấp dịch vụ

    // Enums
    public enum SkillLevel {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }

    // Business methods
    /**
     * Tính tổng giá service (chỉ tiền công lao động)
     */
    public BigDecimal calculateTotalPrice() {
        return laborCost != null ? laborCost : BigDecimal.ZERO;
    }

    /**
     * Cập nhật giá service
     */
    public void updatePricing() {
        this.basePrice = calculateTotalPrice();
    }

    /**
     * Cập nhật thời gian dự kiến từ service process
     */
    public void updateEstimatedDuration() {
        if (serviceProcess != null) {
            this.estimatedDuration = serviceProcess.calculateEstimatedDuration();
        } else {
            this.estimatedDuration = standardDuration;
        }
    }

    /**
     * Kiểm tra xem service có sử dụng quy trình mặc định không
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
}
