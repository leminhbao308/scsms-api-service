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
    @UniqueConstraint(name = "uq_pbi_book_product", columnNames = {"price_book_id", "product_id"}),
    @UniqueConstraint(name = "uq_pbi_book_service", columnNames = {"price_book_id", "service_id"}),
    @UniqueConstraint(name = "uq_pbi_book_service_package", columnNames = {"price_book_id", "service_package_id"})
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
    
    
    // Chỉ một trong ba relationship này được sử dụng
    @ManyToOne(optional = true)
    @JoinColumn(name = "product_id", nullable = true, foreignKey = @ForeignKey(name = "fk_pbi_product"))
    private Product product;
    
    @ManyToOne(optional = true)
    @JoinColumn(name = "service_id", nullable = true, foreignKey = @ForeignKey(name = "fk_pbi_service"))
    private Service service;
    
    @ManyToOne(optional = true)
    @JoinColumn(name = "service_package_id", nullable = true, foreignKey = @ForeignKey(name = "fk_pbi_service_package"))
    private ServicePackage servicePackage;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type", length = 40, nullable = false)
    private PricingPolicyType policyType;
    
    
    // Giá cố định
    @Column(name = "fixed_price", precision = 18, scale = 4)
    private BigDecimal fixedPrice;
    
    
    // Tỉ lệ lợi nhuận trên giá nhập cao nhất
    @Column(name = "markup_percent", precision = 9, scale = 4)
    private BigDecimal markupPercent;
    
    // Business methods
    
    /**
     * Xác định loại item (PRODUCT, SERVICE, SERVICE_PACKAGE)
     */
    public ItemType getItemType() {
        if (product != null) return ItemType.PRODUCT;
        if (service != null) return ItemType.SERVICE;
        if (servicePackage != null) return ItemType.SERVICE_PACKAGE;
        throw new IllegalStateException("PriceBookItem must have exactly one of: product, service, or servicePackage");
    }
    
    /**
     * Lấy ID của item được reference
     */
    public UUID getReferencedItemId() {
        return switch (getItemType()) {
            case PRODUCT -> product.getProductId();
            case SERVICE -> service.getServiceId();
            case SERVICE_PACKAGE -> servicePackage.getPackageId();
        };
    }
    
    /**
     * Lấy tên của item được reference
     */
    public String getReferencedItemName() {
        return switch (getItemType()) {
            case PRODUCT -> product.getProductName();
            case SERVICE -> service.getServiceName();
            case SERVICE_PACKAGE -> servicePackage.getPackageName();
        };
    }
    
    /**
     * Validate rằng chỉ có một trong ba relationship được set
     */
    @PrePersist
    @PreUpdate
    public void validate() {
        int count = 0;
        if (product != null) count++;
        if (service != null) count++;
        if (servicePackage != null) count++;
        
        if (count != 1) {
            throw new IllegalStateException("PriceBookItem must have exactly one of: product, service, or servicePackage");
        }
    }
    
    /**
     * Enum định nghĩa loại item
     */
    public enum ItemType {
        PRODUCT, SERVICE, SERVICE_PACKAGE
    }
}
