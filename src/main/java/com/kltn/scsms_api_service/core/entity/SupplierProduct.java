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
@Table(name = "supplier_product",
    schema = GeneralConstant.DB_SCHEMA_DEV,
    uniqueConstraints = {
    @UniqueConstraint(name = "uq_supplier_product", columnNames = {"supplier_id", "product_id"})
})
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierProduct extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "supplier_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sp_supplier"))
    private Supplier supplier;
    
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sp_product"))
    private Product product;
    
    
    @Column(name = "supplier_sku", length = 100)
    private String supplierSku;
}
