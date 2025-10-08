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
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "purchase_order_line", schema = GeneralConstant.DB_SCHEMA_DEV)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderLine extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pol_po"))
    private PurchaseOrder purchaseOrder;
    
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pol_product"))
    private Product product;
    
    
    // Bắt buộc chọn nhà cung cấp theo từng sản phẩm
    @ManyToOne(optional = false)
    @JoinColumn(name = "supplier_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pol_supplier"))
    private Supplier supplier;
    
    
    @Column(name = "qty_ordered", nullable = false)
    private Long quantityOrdered;
    
    
    @Column(name = "unit_cost", precision = 18, scale = 4, nullable = false)
    private BigDecimal unitCost;
    
    
    @Column(name = "lot_code", length = 100)
    private String lotCode;
    
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
}
