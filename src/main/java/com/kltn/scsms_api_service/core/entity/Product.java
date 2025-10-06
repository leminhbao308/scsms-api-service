package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products", schema = GeneralConstant.DB_SCHEMA_DEV)
public class Product extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    
    @Column(name = "product_url", unique = true, nullable = false, length = 255)
    private String productUrl;
    
    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_type_id")
    private ProductType productType;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(name = "unit_of_measure", nullable = false, length = 50)
    private String unitOfMeasure;
    
    @Column(name = "brand", length = 100)
    private String brand;
    
    @Column(name = "model", length = 100)
    private String model;
    
    @Column(name = "sku", unique = true, length = 100)
    private String sku;
    
    @Column(name = "barcode", length = 100)
    private String barcode;
    
    @Column(name = "peak_price")
    @Builder.Default
    private BigDecimal peakPrice = BigDecimal.ZERO;
    
    // Business Relations
    @Column(name = "supplier_id")
    private UUID supplierId;
    
    // Product Features
    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;
    
    // Relationship with ProductAttribute through ProductAttributeValue
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<ProductAttributeValue> attributeValues = new ArrayList<>();
    
    // Utility methods
    public void addAttributeValue(ProductAttributeValue attributeValue) {
        attributeValues.add(attributeValue);
        attributeValue.setProduct(this);
    }
    
    public void removeAttributeValue(ProductAttributeValue attributeValue) {
        attributeValues.remove(attributeValue);
        attributeValue.setProduct(null);
    }
    
    public ProductAttributeValue getAttributeValue(UUID attributeId) {
        return attributeValues.stream()
                .filter(av -> av.getAttributeId().equals(attributeId))
                .findFirst()
                .orElse(null);
    }
}
