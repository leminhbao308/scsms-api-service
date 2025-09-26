package com.kltn.scsms_api_service.core.dto.productManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateProductRequest {
    
    @Size(min = 2, max = 500, message = "Product name must be between 2 and 500 characters")
    @JsonProperty("product_name")
    private String productName;
    
    @Size(max = 1000, message = "Product URL must not exceed 1000 characters")
    @JsonProperty("product_url")
    private String productUrl;
    
    @JsonProperty("category_id")
    private UUID categoryId;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
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
    
    @JsonProperty("specifications")
    private String specifications;
    
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
    
    @Min(value = 0, message = "Minimum stock level must be non-negative")
    @JsonProperty("min_stock_level")
    private Integer minStockLevel;
    
    @Min(value = 0, message = "Maximum stock level must be non-negative")
    @JsonProperty("max_stock_level")
    private Integer maxStockLevel;
    
    @Min(value = 0, message = "Reorder point must be non-negative")
    @JsonProperty("reorder_point")
    private Integer reorderPoint;
    
    @DecimalMin(value = "0.0", message = "Weight must be non-negative")
    @JsonProperty("weight")
    private BigDecimal weight;
    
    @JsonProperty("dimensions")
    private String dimensions;
    
    @Min(value = 0, message = "Warranty period must be non-negative")
    @JsonProperty("warranty_period_months")
    private Integer warrantyPeriodMonths;
    
    @JsonProperty("is_trackable")
    private Boolean isTrackable;
    
    @JsonProperty("is_consumable")
    private Boolean isConsumable;
    
    @JsonProperty("image_urls")
    private String imageUrls;
    
    @JsonProperty("tags")
    private String tags;
    
    @JsonProperty("supplier_id")
    private UUID supplierId;
    
    @JsonProperty("is_featured")
    private Boolean isFeatured;
    
    @Min(value = 0, message = "Sort order must be non-negative")
    @JsonProperty("sort_order")
    private Integer sortOrder;
    
    @JsonProperty("is_active")
    private Boolean isActive;
}