package com.kltn.scsms_api_service.core.dto.productManagement;

import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductInfoDto {
    
    private UUID productId;
    private String productUrl;
    private String productName;
    private UUID categoryId;
    private String categoryName;
    private String description;
    private String unitOfMeasure;
    private String brand;
    private String model;
    private String specifications;
    private String sku;
    private String barcode;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private Integer reorderPoint;
    private BigDecimal weight;
    private String dimensions;
    private Integer warrantyPeriodMonths;
    private Boolean isTrackable;
    private Boolean isConsumable;
    private String imageUrls;
    private String tags;
    private UUID supplierId;
    private Boolean isFeatured;
    private Integer sortOrder;
    private Boolean isActive;
    private AuditDto audit;
}