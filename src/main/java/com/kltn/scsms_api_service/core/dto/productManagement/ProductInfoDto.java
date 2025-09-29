package com.kltn.scsms_api_service.core.dto.productManagement;

import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductInfoDto extends AuditDto {
    
    private UUID productId;
    private String productUrl;
    private String productName;
    private UUID categoryId;
    private String categoryName;
    private String description;
    private String unitOfMeasure;
    private String brand;
    private String model;
    private String sku;
    private String barcode;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    
    // Inventory Management
    private Integer minStockLevel;
    private Integer maxStockLevel;
    
    // Physical Properties
    private BigDecimal weight; // in kg
    private Map<String, String> dimensions; // {"length": "10cm", "width": "5cm", "height": "3cm"}
    
    // Warranty Information
    private Integer warrantyPeriodMonths;
    
    // Media and Display
    private Map<String, String> imageUrls; // {"main": "url1", "thumbnail": "url2"}
    
    // Business Relations
    private UUID supplierId;
    
    // Product Features
    private Boolean isFeatured;
    private Boolean isActive;
    
    // Additional Specifications
    private Map<String, String> specifications; // {"brightness": "3000lm", "color_temp": "6000K", "power": "50W"}
}