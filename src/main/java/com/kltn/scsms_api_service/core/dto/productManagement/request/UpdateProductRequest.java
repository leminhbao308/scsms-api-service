package com.kltn.scsms_api_service.core.dto.productManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateProductRequest {
    
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    @JsonProperty("product_name")
    private String productName;
    
    @Size(max = 255, message = "Product URL must not exceed 255 characters")
    @JsonProperty("product_url")
    private String productUrl;
    
    @JsonProperty("product_type_id")
    private UUID productTypeId;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @JsonProperty("description")
    private String description;
    
    @Size(max = 50, message = "Unit of measure must not exceed 50 characters")
    @JsonProperty("unit_of_measure")
    private String unitOfMeasure;
    
    @Size(max = 100, message = "Brand must not exceed 100 characters")
    @JsonProperty("brand")
    private String brand;
    
    @Size(max = 100, message = "Model must not exceed 100 characters")
    @JsonProperty("model")
    private String model;
    
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    @JsonProperty("sku")
    private String sku;
    
    @Size(max = 100, message = "Barcode must not exceed 100 characters")
    @JsonProperty("barcode")
    private String barcode;
    
    
    // Business Relations
    @JsonProperty("supplier_id")
    private UUID supplierId;
    
    // Product Features
    @JsonProperty("is_featured")
    private Boolean isFeatured;
    
    // Status
    @JsonProperty("is_active")
    private Boolean isActive;
    
    // Product Attribute Values
    @Valid
    @JsonProperty("attribute_values")
    private List<ProductAttributeValueRequest> attributeValues;
}