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
    
    @JsonProperty("product_name")
    private String productName;
    
    @JsonProperty("product_url")
    private String productUrl;
    
    @JsonProperty("category_id")
    private UUID categoryId;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("unit_of_measure")
    private String unitOfMeasure;
    
    @JsonProperty("brand")
    private String brand;
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("specifications")
    private Map<String, String> specifications;
    
    @JsonProperty("sku")
    private String sku;
    
    @JsonProperty("barcode")
    private String barcode;
    
    @JsonProperty("cost_price")
    private BigDecimal costPrice;
    
    @JsonProperty("selling_price")
    private BigDecimal sellingPrice;
    
    @JsonProperty("min_stock_level")
    private Integer minStockLevel;
    
    @JsonProperty("max_stock_level")
    private Integer maxStockLevel;
    
    @JsonProperty("reorder_point")
    private Integer reorderPoint;
    
    @JsonProperty("weight")
    private BigDecimal weight;
    
    @JsonProperty("dimensions")
    private Map<String, String> dimensions;
    
    @JsonProperty("warranty_period_months")
    private Integer warrantyPeriodMonths;
    
    @JsonProperty("is_trackable")
    private Boolean isTrackable;
    
    @JsonProperty("is_consumable")
    private Boolean isConsumable;
    
    @JsonProperty("image_urls")
    private Map<String, String> imageUrls;
    
    @JsonProperty("tags")
    private Map<String, String> tags;
    
    @JsonProperty("supplier_id")
    private UUID supplierId;
    
    @JsonProperty("is_featured")
    private Boolean isFeatured;
    
    @JsonProperty("sort_order")
    private Integer sortOrder;
}
