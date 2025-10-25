package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "promotion_usages", schema = GeneralConstant.DB_SCHEMA_DEV)
public class PromotionUsage extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "usage_id", nullable = false)
    private UUID usageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_line_id")
    private PromotionLine promotionLine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer; // customer sử dụng KM (nếu có)

    @Column(name = "order_id")
    private UUID orderId; // order tham chiếu

    @Column(name = "coupon_code", length = 100)
    private String couponCode;

    @Column(name = "discount_amount", precision = 18, scale = 4, nullable = false)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "used_at", nullable = false)
    @Builder.Default
    private LocalDateTime usedAt = LocalDateTime.now();

    // ===== PROMOTION SNAPSHOT FOR USAGE HISTORY =====

    /**
     * Snapshot of promotion details at the time of usage
     * Stores complete promotion information including discount_lines, rules, etc.
     */
    @Column(name = "promotion_snapshot", columnDefinition = "TEXT")
    private String promotionSnapshot;

    /**
     * Original order amount before discount
     */
    @Column(name = "order_original_amount", precision = 18, scale = 4)
    private BigDecimal orderOriginalAmount;

    /**
     * Final order amount after all discounts
     */
    @Column(name = "order_final_amount", precision = 18, scale = 4)
    private BigDecimal orderFinalAmount;

    /**
     * Branch where the promotion was used
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;
}
