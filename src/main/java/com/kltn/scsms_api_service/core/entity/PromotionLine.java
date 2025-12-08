package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "promotion_lines", schema = GeneralConstant.DB_SCHEMA_DEV)
public class PromotionLine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "promotion_line_id", nullable = false)
    private UUID promotionLineId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;
    
    @Column(name = "line_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private LineType lineType; // PRODUCT | CATEGORY | SERVICE | PACKAGE | ORDER | CUSTOMER_GROUP | ALL
    
    @Column(name = "target_id")
    private UUID targetId; // product_id / category_id / service_id (NULL nếu line_type = ALL or ORDER)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch; // override: chỉ áp dụng ở branch này
    
    @Column(name = "discount_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private DiscountType discountType; // PERCENT | AMOUNT | BUY_X_GET_Y | FREE_PRODUCT | FIXED_PRICE
    
    @Column(name = "discount_value", precision = 18, scale = 4)
    private BigDecimal discountValue; // % (10 = 10%) hoặc số tiền
    
    @Column(name = "max_discount_amount", precision = 18, scale = 4)
    private BigDecimal maxDiscountAmount; // giới hạn giảm tối đa (áp dụng cho %)
    
    @Column(name = "min_order_value", precision = 18, scale = 4)
    private BigDecimal minOrderValue; // điều kiện cho order-level
    
    @Column(name = "min_quantity")
    private Integer minQuantity; // điều kiện cho product qty
    
    @Column(name = "buy_qty")
    private Integer buyQty; // cho BUY_X_GET_Y
    
    @Column(name = "get_qty")
    private Integer getQty; // cho BUY_X_GET_Y
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "free_product_id")
    private Product freeProduct; // cho FREE_PRODUCT
    
    @Column(name = "free_quantity")
    @Builder.Default
    private Integer freeQuantity = 1;
    
    @Column(name = "start_at")
    private LocalDateTime startAt; // optional override line-level thời gian
    
    @Column(name = "end_at")
    private LocalDateTime endAt;
    
    @Column(name = "line_priority")
    @Builder.Default
    private Integer linePriority = 100; // để sắp xếp khi cùng áp dụng nhiều line
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Cần thêm method validate:
    public boolean isApplicableToItem(UUID itemId, int quantity, BigDecimal itemAmount) {
        if (!isActive) return false;
        
        // Check time override
        if (startAt != null && LocalDateTime.now().isBefore(startAt)) return false;
        if (endAt != null && LocalDateTime.now().isAfter(endAt)) return false;
        
        // Check target
        if (lineType != LineType.ALL) {
            if (targetId != null && !targetId.equals(itemId)) return false;
        }
        
        // Check min quantity
        if (minQuantity != null && quantity < minQuantity) return false;
        
        return true;
    }
    
    // PromotionLine cần method:
    public BigDecimal calculateDiscount(BigDecimal originalAmount, int quantity) {
        BigDecimal discount = BigDecimal.ZERO;
    
        switch (discountType) {
            case PERCENT:
                discount = originalAmount.multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
                if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
                    discount = maxDiscountAmount;
                }
                break;
            case AMOUNT:
                discount = discountValue.min(originalAmount);
                break;
            case FIXED_PRICE:
                discount = originalAmount.subtract(discountValue).max(BigDecimal.ZERO);
                break;
            case BUY_X_GET_Y:
                if (buyQty != null && getQty != null && buyQty > 0 && getQty > 0) {
                    int eligibleSets = quantity / (buyQty + getQty);
                    int freeItems = eligibleSets * getQty;
                    if (freeItems > 0) {
                        // Assume discountValue is the price per item
                        discount = discountValue.multiply(BigDecimal.valueOf(freeItems));
                    }
                }
                break;
            case FREE_PRODUCT:
                if (freeProduct != null && freeQuantity != null && freeQuantity > 0) {
                    // Assume discountValue is the price per free product
                    discount = discountValue.multiply(BigDecimal.valueOf(freeQuantity));
                }
                break;
            default:
        }
    
        return discount.max(BigDecimal.ZERO);
    }
    
    // === ENUMS ===
    public enum LineType {
        PRODUCT, CATEGORY, SERVICE, ALL
    }
    
    public enum DiscountType {
        PERCENT, AMOUNT, BUY_X_GET_Y, FREE_PRODUCT, FIXED_PRICE
    }
}
