package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PricingPolicyType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "price_book_item",
    schema = GeneralConstant.DB_SCHEMA_DEV,
    uniqueConstraints = {
    @UniqueConstraint(name = "uq_pbi_book_product", columnNames = {"price_book_id", "product_id"})
})
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PriceBookItem extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "price_book_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pbi_book"))
    private PriceBook priceBook;
    
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pbi_product"))
    private Product product;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "service_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pbi_service"))
    private Service service;
    
    
    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type", length = 40, nullable = false)
    private PricingPolicyType policyType;
    
    
    // Giá cố định
    @Column(name = "fixed_price", precision = 18, scale = 4)
    private BigDecimal fixedPrice;
    
    
    // Tỉ lệ lợi nhuận trên giá nhập cao nhất
    @Column(name = "markup_percent", precision = 9, scale = 4)
    private BigDecimal markupPercent;
}
