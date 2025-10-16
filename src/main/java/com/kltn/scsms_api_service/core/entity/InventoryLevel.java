package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "inventory_level",
    schema = GeneralConstant.DB_SCHEMA_DEV,
    uniqueConstraints = {
    @UniqueConstraint(name = "uq_inventory_level", columnNames = {"branch_id", "product_id"})
})
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryLevel extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "branch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_il_branch"))
    private Branch branch;
    
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_il_product"))
    private Product product;
    
    
    @Column(name = "on_hand", nullable = false)
    @Builder.Default
    private Long onHand = 0L;
    
    
    @Column(name = "reserved", nullable = false)
    @Builder.Default
    private Long reserved = 0L;
    
    
    @Transient
        public Long getAvailable() {
        long safeOnHand = (onHand != null) ? onHand : 0L;
        long safeReserved = (reserved != null) ? reserved : 0L;
        return Math.max(0L, safeOnHand - safeReserved);
    }
}
