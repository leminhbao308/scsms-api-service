package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "promotions", schema = GeneralConstant.DB_SCHEMA_DEV)
public class Promotion extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "promotion_id", nullable = false)
    private UUID promotionId;
    
    @Column(name = "promotion_code", unique = true, length = 100)
    private String promotionCode; // coupon code hoặc mã nội bộ
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;
    
    @Column(name = "start_at")
    private LocalDateTime startAt; // bắt đầu
    
    @Column(name = "end_at")
    private LocalDateTime endAt; // kết thúc
    
    @Column(name = "usage_limit")
    private Long usageLimit; // tổng số lần dùng (NULL = unlimited)
    
    @Column(name = "per_customer_limit")
    private Long perCustomerLimit; // số lần cho mỗi khách hàng (NULL = unlimited)
    
    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 100; // ưu tiên (lower = ưu tiên cao)
    
    @Column(name = "is_stackable")
    @Builder.Default
    private Boolean isStackable = false; // có cộng dồn với KM khác hay không
    
    @Column(name = "coupon_redeem_once")
    @Builder.Default
    private Boolean couponRedeemOnce = false; // dùng 1 lần mã coupon (nếu coupon)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch; // nếu chỉ áp dụng cho 1 chi nhánh
    
    
    // === RELATIONSHIPS ===
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PromotionUsage> usages = new java.util.ArrayList<>();
    
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PromotionLine> promotionLines = new java.util.ArrayList<>();
    
    // === VALIDATION METHODS ===
    @AssertTrue(message = "End date must be after start date")
    private boolean isValidDateRange() {
        return endAt == null || startAt == null || endAt.isAfter(startAt);
    }
    
    // === BUSINESS METHODS ===
    /**
     * Kiểm tra khuyến mãi có thể áp dụng
     */
    public boolean isApplicable(UUID customerId, BigDecimal orderAmount, List<UUID> serviceIds, List<UUID> productIds) {
        // Kiểm tra thời gian
        if (!isWithinValidPeriod()) return false;
        
        // Kiểm tra số lần sử dụng
        if (isUsageLimitExceeded()) return false;
        
        return true;
    }
    
    /**
     * Kiểm tra trong thời gian hiệu lực
     */
    public boolean isWithinValidPeriod() {
        LocalDateTime now = LocalDateTime.now();
        return (startAt == null || now.isAfter(startAt)) && (endAt == null || now.isBefore(endAt));
    }
    
    /**
     * Kiểm tra đã vượt quá giới hạn sử dụng
     */
    public boolean isUsageLimitExceeded() {
        return usageLimit != null && usages.size() >= usageLimit;
    }
    
    /**
     * Kiểm tra có thể stack với khuyến mãi khác
     */
    public boolean canStackWith(Promotion other) {
        return this.isStackable && other.isStackable;
    }
}
