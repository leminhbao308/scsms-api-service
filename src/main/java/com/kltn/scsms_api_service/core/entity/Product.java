package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
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
    
    @Column(name = "product_url", unique = true, nullable = false, length = 255)
    private String productUrl;
    
    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
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
    
    @Column(name = "cost_price", precision = 15, scale = 2)
    private BigDecimal costPrice;
    
    @Column(name = "selling_price", precision = 15, scale = 2)
    private BigDecimal sellingPrice;
    
    // Inventory Management
    @Column(name = "min_stock_level")
    @Builder.Default
    private Integer minStockLevel = 0;
    
    @Column(name = "max_stock_level")
    private Integer maxStockLevel;
    
    // Physical Properties
    @Column(name = "weight", precision = 10, scale = 3)
    private BigDecimal weight; // in kg
    
    @Column(name = "dimensions")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> dimensions; // {"length": "10cm", "width": "5cm", "height": "3cm"}
    
    // Warranty Information
    @Column(name = "warranty_period_months")
    private Integer warrantyPeriodMonths;
    
    // Media and Display
    @Column(name = "image_urls")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> imageUrls; // {"main": "url1", "thumbnail": "url2", "gallery": ["url3", "url4"]}
    
    // Business Relations
    @Column(name = "supplier_id")
    private UUID supplierId;
    
    // Product Features
    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;
    
    // Additional Specifications (for complex products like LED lights)
    @Column(name = "specifications")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> specifications; // {"brightness": "3000lm", "color_temp": "6000K", "power": "50W"}
}