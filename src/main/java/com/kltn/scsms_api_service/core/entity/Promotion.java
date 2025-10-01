package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    
    @Column(name = "promotion_code", unique = true, nullable = false, length = 50)
    private String promotionCode;
    
    @Column(name = "promotion_name", nullable = false, length = 255)
    private String promotionName;
    
    @Column(name = "promotion_type", nullable = false, length = 255)
    private String promotionType; // PERCENTAGE, FIXED_AMOUNT, FREE_ITEM, FREE_SERVICE, BUY_X_GET_Y, etc.
    
    @Column(name = "description", length = 1000)
    private String description;
    
    // === CATEGORY RELATIONSHIP ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category; // Liên kết với Category (type: PROMOTION_SYS)
    
    // === PROMOTION VALUE & CONDITIONS ===
    @Column(name = "discount_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;
    
    @Column(name = "discount_value", precision = 15, scale = 2)
    private BigDecimal discountValue; // Giá trị khuyến mãi (%, VND, số lượng)
    
    @Column(name = "max_discount_amount", precision = 15, scale = 2)
    private BigDecimal maxDiscountAmount; // Số tiền giảm tối đa (cho %)
    
    @Column(name = "min_order_amount", precision = 15, scale = 2)
    private BigDecimal minOrderAmount; // Đơn hàng tối thiểu để áp dụng
    
    // === VALIDITY PERIOD ===
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;
    
    // === USAGE LIMITS ===
    @Column(name = "usage_limit")
    private Integer usageLimit; // Tổng số lần sử dụng (null = không giới hạn)
    
    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0; // Số lần đã sử dụng
    
    @Column(name = "usage_limit_per_customer")
    private Integer usageLimitPerCustomer; // Giới hạn sử dụng per customer
    
    // === TARGET CONDITIONS ===
    @Column(name = "target_customer_ranks")
    @JdbcTypeCode(SqlTypes.JSON)
    private String targetCustomerRanks; // JSON array: ["GOLD", "SILVER"]
    
    @Column(name = "target_vehicle_types")
    @JdbcTypeCode(SqlTypes.JSON)
    private String targetVehicleTypes; // JSON array: ["SEDAN", "SUV"]
    
    @Column(name = "target_services")
    @JdbcTypeCode(SqlTypes.JSON)
    private String targetServices; // JSON array: service IDs
    
    @Column(name = "target_products")
    @JdbcTypeCode(SqlTypes.JSON)
    private String targetProducts; // JSON array: product IDs
    
    @Column(name = "target_branches")
    @JdbcTypeCode(SqlTypes.JSON)
    private String targetBranches; // JSON array: branch IDs
    
    // === FREE ITEM SPECIFIC ===
    @Column(name = "free_item_quantity")
    private Integer freeItemQuantity; // Số lượng item miễn phí
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "free_product_id")
    private Product freeProduct; // Sản phẩm miễn phí
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "free_service_id")
    private Service freeService; // Dịch vụ miễn phí
    
    // === BUY X GET Y SPECIFIC ===
    @Column(name = "buy_quantity")
    private Integer buyQuantity; // Mua X
    
    @Column(name = "get_quantity")
    private Integer getQuantity; // Được Y
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buy_product_id")
    private Product buyProduct; // Sản phẩm cần mua
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "get_product_id")
    private Product getProduct; // Sản phẩm được tặng
    
    // === STATUS & VISIBILITY ===
    @Column(name = "is_visible", nullable = false)
    @Builder.Default
    private Boolean isVisible = true; // Hiển thị cho customer
    
    @Column(name = "priority")
    private Integer priority; // Độ ưu tiên (cao hơn = ưu tiên hơn)
    
    // === MEDIA ===
    @Column(name = "image_urls")
    @JdbcTypeCode(SqlTypes.JSON)
    private String imageUrls; // JSON array: ["url1", "url2"]
    
    @Column(name = "banner_url")
    private String bannerUrl; // URL banner khuyến mãi
    
    // === BUSINESS RULES ===
    @Column(name = "stackable", nullable = false)
    @Builder.Default
    private Boolean stackable = false; // Có thể kết hợp với khuyến mãi khác
    
    @Column(name = "auto_apply", nullable = false)
    @Builder.Default
    private Boolean autoApply = false; // Tự động áp dụng khi đủ điều kiện
    
    @Column(name = "require_coupon_code", nullable = false)
    @Builder.Default
    private Boolean requireCouponCode = true; // Yêu cầu nhập mã khuyến mãi
    
    // === RELATIONSHIPS ===
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PromotionUsage> usages = new java.util.ArrayList<>();
    
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PromotionLine> promotionLines = new java.util.ArrayList<>();
    
    // === ENUMS ===
    public enum DiscountType {
        PERCENTAGE,           // Giảm %
        FIXED_AMOUNT,         // Giảm số tiền
        FREE_ITEM,            // Tặng item
        FREE_SERVICE          // Tặng dịch vụ
    }
    
    // === VALIDATION METHODS ===
    @AssertTrue(message = "End date must be after start date")
    private boolean isValidDateRange() {
        return endDate == null || startDate == null || endDate.isAfter(startDate);
    }
    
    @AssertTrue(message = "Usage limit must be greater than used count")
    private boolean isValidUsageLimit() {
        return usageLimit == null || usageLimit >= usedCount;
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
        
        // Kiểm tra điều kiện đơn hàng tối thiểu
        if (minOrderAmount != null && orderAmount.compareTo(minOrderAmount) < 0) return false;
        
        // Kiểm tra target conditions
        if (!matchesTargetConditions(customerId, serviceIds, productIds)) return false;
        
        return true;
    }
    
    /**
     * Tính toán giá trị khuyến mãi
     */
    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        switch (discountType) {
            case PERCENTAGE:
                BigDecimal discount = orderAmount.multiply(discountValue.divide(BigDecimal.valueOf(100)));
                return maxDiscountAmount != null ? 
                    discount.min(maxDiscountAmount) : discount;
            case FIXED_AMOUNT:
                return discountValue.min(orderAmount);
            default:
                return BigDecimal.ZERO;
        }
    }
    
    /**
     * Kiểm tra trong thời gian hiệu lực
     */
    public boolean isWithinValidPeriod() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startDate) && now.isBefore(endDate);
    }
    
    /**
     * Kiểm tra đã vượt quá giới hạn sử dụng
     */
    public boolean isUsageLimitExceeded() {
        return usageLimit != null && usedCount >= usageLimit;
    }
    
    /**
     * Kiểm tra điều kiện target
     */
    private boolean matchesTargetConditions(UUID customerId, List<UUID> serviceIds, List<UUID> productIds) {
        // TODO: Implement target condition matching logic
        // This would involve checking customer rank, vehicle types, services, products, branches
        return true; // Simplified for now
    }
    
    /**
     * Tăng số lần sử dụng
     */
    public void incrementUsage() {
        this.usedCount++;
    }
    
    /**
     * Kiểm tra có thể stack với khuyến mãi khác
     */
    public boolean canStackWith(Promotion other) {
        return this.stackable && other.stackable;
    }
}
