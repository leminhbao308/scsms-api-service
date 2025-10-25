package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import com.kltn.scsms_api_service.core.entity.enumAttribute.SalesStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sales_order", schema = GeneralConstant.DB_SCHEMA_DEV)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrder extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Customer order
    @ManyToOne
    @JoinColumn(name = "customer_id", foreignKey = @ForeignKey(name = "fk_po_customer"))
    private User customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "branch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_so_branch"))
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40, nullable = false)
    private SalesStatus status;

    // ===== DISCOUNT TRACKING FIELDS =====

    // Original amount before any discounts (sum of all line items at regular price)
    @Column(name = "original_amount", precision = 18, scale = 4)
    private BigDecimal originalAmount;

    // Total discount amount (sum of all promotion discounts applied)
    @Column(name = "total_discount_amount", precision = 18, scale = 4)
    private BigDecimal totalDiscountAmount;

    // Final amount after discounts (original_amount - total_discount_amount)
    // This is the actual amount customer needs to pay
    @Column(name = "final_amount", precision = 18, scale = 4)
    private BigDecimal finalAmount;

    // Overall discount percentage (if applicable, for display purposes)
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    // JSON snapshot of promotions applied (saved at order creation time)
    // Format: [{"promotion_id":"uuid","code":"SUMMER24","name":"Giảm giá mùa hè",
    // "discount_type":"PERCENT","discount_value":20,"description":"..."}]
    // This preserves promotion details even if promotion is later modified/deleted
    @Column(name = "promotion_snapshot", columnDefinition = "TEXT")
    private String promotionSnapshot;

    // ===== CANCELLATION TRACKING =====

    /**
     * Reason for order cancellation (required when status = CANCELED)
     * Max length: 500 characters
     */
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalesOrderLine> lines = new ArrayList<>();
}
