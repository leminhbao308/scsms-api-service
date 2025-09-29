package com.kltn.scsms_api_service.core.dto.productManagement;

import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    private Map<String, String> specifications;
    private String sku;
    private String barcode;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private Integer reorderPoint;
    private BigDecimal weight;
    private Map<String, String> dimensions;
    private Integer warrantyPeriodMonths;
    private Boolean isTrackable;
    private Boolean isConsumable;
    private Map<String, String> imageUrls;
    private Map<String, String> tags;
    private UUID supplierId;
    private Boolean isFeatured;
    private Integer sortOrder;
    private Boolean isActive;
}
