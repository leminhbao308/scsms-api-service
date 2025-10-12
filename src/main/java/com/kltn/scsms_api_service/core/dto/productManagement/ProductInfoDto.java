package com.kltn.scsms_api_service.core.dto.productManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductInfoDto extends AuditDto {
    
    @JsonProperty("product_id")
    private UUID productId;
    
    @JsonProperty("product_url")
    private String productUrl;
    
    @JsonProperty("product_name")
    private String productName;
    
    @JsonProperty("product_type_id")
    private UUID productTypeId;
    
    @JsonProperty("product_type_name")
    private String productTypeName;
    
    private String description;
    
    @JsonProperty("unit_of_measure")
    private String unitOfMeasure;
    
    private String brand;
    private String model;
    private String sku;
    private String barcode;
    
    @JsonProperty("peak_price")
    private BigDecimal peakPrice;
    
    // Business Relations
    @JsonProperty("supplier_id")
    private UUID supplierId;
    
    // Product Features
    @JsonProperty("is_featured")
    private Boolean isFeatured;
    
    // Note: isActive is inherited from AuditDto with @JsonProperty("is_active")
    
    // Product Attribute Values
    @JsonProperty("attribute_values")
    private List<ProductAttributeValueDto> attributeValues;
}
