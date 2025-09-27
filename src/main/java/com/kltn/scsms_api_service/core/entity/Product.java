package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.core.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;
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
    
    @Column(name = "product_url", unique = true, nullable = false, length = Integer.MAX_VALUE)
    private String productUrl;
    
    @Column(name = "product_name", nullable = false, length = Integer.MAX_VALUE)
    private String productName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;
    
    @Column(name = "unit_of_measure", nullable = false)
    private String unitOfMeasure;
    
    @Column(name = "brand")
    private String brand;
    
    @Column(name = "model")
    private String model;
    
    @Column(name = "specifications")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> specifications;
    
    @Column(name = "sku", unique = true)
    private String sku;
    
    @Column(name = "barcode")
    private String barcode;
    
    @Column(name = "cost_price", precision = 15, scale = 2)
    private BigDecimal costPrice;
    
    @Column(name = "selling_price", precision = 15, scale = 2)
    private BigDecimal sellingPrice;
    
    @Column(name = "min_stock_level")
    @Builder.Default
    private Integer minStockLevel = 0;
    
    @Column(name = "max_stock_level")
    private Integer maxStockLevel;
    
    @Column(name = "reorder_point")
    private Integer reorderPoint;
    
    @Column(name = "weight")
    private BigDecimal weight;
    
    @Column(name = "dimensions")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> dimensions;
    
    @Column(name = "warranty_period_months")
    private Integer warrantyPeriodMonths;
    
    @Column(name = "is_trackable")
    @Builder.Default
    private Boolean isTrackable = false;
    
    @Column(name = "is_consumable")
    @Builder.Default
    private Boolean isConsumable = true;
    
    @Column(name = "image_urls")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> imageUrls;
    
    @Column(name = "tags")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> tags;
    
    @Column(name = "supplier_id")
    private UUID supplierId;
    
    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;
    
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
}
