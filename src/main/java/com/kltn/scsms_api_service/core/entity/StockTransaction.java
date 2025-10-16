package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import com.kltn.scsms_api_service.core.entity.enumAttribute.StockRefType;
import com.kltn.scsms_api_service.core.entity.enumAttribute.StockTxnType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "stock_txn",
    schema = GeneralConstant.DB_SCHEMA_DEV,
    indexes = {
        @Index(name = "idx_txn_branch", columnList = "branch_id"),
        @Index(name = "idx_txn_product", columnList = "product_id"),
        @Index(name = "idx_txn_created", columnList = "createdDate")
    })
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransaction extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "branch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_txn_branch"))
    private Branch branch;
    
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_txn_product"))
    private Product product;
    
    
    @ManyToOne
    @JoinColumn(name = "inventory_lot_id", foreignKey = @ForeignKey(name = "fk_txn_lot"))
    private InventoryLot inventoryLot;
    
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 40, nullable = false)
    private StockTxnType type;
    
    
    @Column(name = "qty", nullable = false)
        /**
     * Quantity of product moved in this transaction.
     * Positive values indicate stock being added to the warehouse (stock in).
     * Negative values indicate stock being removed from the warehouse (stock out).
     */
    private Long quantity;
    
    
    @Column(name = "unit_cost", precision = 18, scale = 4)
    private BigDecimal unitCost;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "ref_type", length = 40)
    private StockRefType refType;
    
    
    @Column(name = "ref_id")
    private UUID refId;
}
