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
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "product_cost_stats",
    schema = GeneralConstant.DB_SCHEMA_DEV,
    uniqueConstraints = {
    @UniqueConstraint(name = "uq_pcs_product", columnNames = {"product_id"})
})
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCostStats extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    
    @OneToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pcs_product"))
    private Product product;
    
    
    // Rule #2: lưu giá nhập cao nhất từng biết
    @Column(name = "peak_purchase_price", precision = 18, scale = 4, nullable = false)
    private BigDecimal peakPurchasePrice;
}
