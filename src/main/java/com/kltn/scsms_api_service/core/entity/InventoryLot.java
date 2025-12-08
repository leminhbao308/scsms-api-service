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
@Table(name = "inventory_lot",
    schema = GeneralConstant.DB_SCHEMA_DEV,
    indexes = {
        @Index(name = "idx_lot_product", columnList = "product_id"),
        @Index(name = "idx_lot_branch", columnList = "branch_id"),
    })
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryLot extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "branch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lot_branch"))
    private Branch branch;
    
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lot_product"))
    private Product product;
    
    
    @ManyToOne
    @JoinColumn(name = "supplier_id", foreignKey = @ForeignKey(name = "fk_lot_supplier"))
    private Supplier supplier;
    
    
    @Column(name = "lot_code", length = 100)
    private String lotCode;
    
    
    @Column(name = "received_at")
    private LocalDateTime receivedAt;
    
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    
    @Column(name = "unit_cost", precision = 18, scale = 4, nullable = false)
    private BigDecimal unitCost;
    
    
    @Column(name = "qty", nullable = false)
    @Builder.Default
    private Long quantity = 0L;
}
