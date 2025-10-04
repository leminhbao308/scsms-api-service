package com.kltn.scsms_api_service.core.dto.productManagement;

import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
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
    private UUID productTypeId;
    private String productTypeName;
    private String description;
    private String unitOfMeasure;
    private String brand;
    private String model;
    private String sku;
    private String barcode;
    
    
    // Business Relations
    private UUID supplierId;
    
    // Product Features
    private Boolean isFeatured;
    private Boolean isActive;
    
    // Product Attribute Values
    private List<ProductAttributeValueDto> attributeValues;
}