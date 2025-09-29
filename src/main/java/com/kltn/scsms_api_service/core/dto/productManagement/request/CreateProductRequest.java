package com.kltn.scsms_api_service.core.dto.productManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateProductRequest {
    
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    @JsonProperty("product_name")
    private String productName;
    
    @NotBlank(message = "Product URL is required")
    @Size(max = 255, message = "Product URL must not exceed 255 characters")
    @JsonProperty("product_url")
    private String productUrl;
    
    @JsonProperty("category_id")
    private UUID categoryId;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @JsonProperty("description")
    private String description;
    
    @NotBlank(message = "Unit of measure is required")
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
    
    @DecimalMin(value = "0.0", message = "Cost price must be non-negative")
    @JsonProperty("cost_price")
    private BigDecimal costPrice;
    
    @DecimalMin(value = "0.0", message = "Selling price must be non-negative")
    @JsonProperty("selling_price")
    private BigDecimal sellingPrice;
    
    // Inventory Management
    @Min(value = 0, message = "Minimum stock level must be non-negative")
    @JsonProperty("min_stock_level")
    private Integer minStockLevel;
    
    @Min(value = 0, message = "Maximum stock level must be non-negative")
    @JsonProperty("max_stock_level")
    private Integer maxStockLevel;
    
    // Physical Properties
    @DecimalMin(value = "0.0", message = "Weight must be non-negative")
    @JsonProperty("weight")
    private BigDecimal weight; // in kg
    
    @JsonProperty("dimensions")
    private Map<String, String> dimensions; // {"length": "10cm", "width": "5cm", "height": "3cm"}
    
    // Warranty Information
    @Min(value = 0, message = "Warranty period must be non-negative")
    @JsonProperty("warranty_period_months")
    private Integer warrantyPeriodMonths;
    
    // Media and Display
    @JsonProperty("image_urls")
    private Map<String, String> imageUrls; // {"main": "url1", "thumbnail": "url2"}
    
    // Business Relations
    @JsonProperty("supplier_id")
    private UUID supplierId;
    
    // Product Features
    @JsonProperty("is_featured")
    private Boolean isFeatured;
    
    // Additional Specifications
    @JsonProperty("specifications")
    private Map<String, String> specifications; // {"brightness": "3000lm", "color_temp": "6000K", "power": "50W"}
}