package com.kltn.scsms_api_service.core.dto.productManagement.param;

import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductFilterParam extends BaseFilterParam<ProductFilterParam> {
    
    // Basic filters
    private UUID categoryId;
    private UUID supplierId;
    private String brand;
    private String model;
    private String sku;
    private String barcode;
    
    // Price filters
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minCostPrice;
    private BigDecimal maxCostPrice;
    
    // Stock filters
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private Integer minReorderPoint;
    private Integer maxReorderPoint;
    private Boolean lowStock;
    
    // Weight filters
    private BigDecimal minWeight;
    private BigDecimal maxWeight;
    
    // Warranty filters
    private Integer minWarrantyMonths;
    private Integer maxWarrantyMonths;
    private Boolean hasWarranty;
    
    // Boolean filters
    private Boolean isTrackable;
    private Boolean isConsumable;
    private Boolean isFeatured;
    private Boolean isActive;
    
    // Search filters
    private String search;
    private String productName;
    private String description;
    private String tag;
    
    // Date filters
    private LocalDateTime createdDateFrom;
    private LocalDateTime createdDateTo;
    private LocalDateTime modifiedDateFrom;
    private LocalDateTime modifiedDateTo;
    
    // Sort options
    @Builder.Default
    private String sort = "productName";
    @Builder.Default
    private String direction = "ASC";
    
    @Override
    protected void standardizeSpecificFields(ProductFilterParam filterParam) {
        // Standardize search
        if (filterParam.getSearch() != null) {
            filterParam.setSearch(filterParam.getSearch().trim());
        }
        if (filterParam.getProductName() != null) {
            filterParam.setProductName(filterParam.getProductName().trim());
        }
        if (filterParam.getDescription() != null) {
            filterParam.setDescription(filterParam.getDescription().trim());
        }
        if (filterParam.getTag() != null) {
            filterParam.setTag(filterParam.getTag().trim());
        }
        if (filterParam.getBrand() != null) {
            filterParam.setBrand(filterParam.getBrand().trim());
        }
        if (filterParam.getModel() != null) {
            filterParam.setModel(filterParam.getModel().trim());
        }
        if (filterParam.getSku() != null) {
            filterParam.setSku(filterParam.getSku().trim());
        }
        if (filterParam.getBarcode() != null) {
            filterParam.setBarcode(filterParam.getBarcode().trim());
        }
    }
    
    @Override
    protected String getDefaultSortField() {
        return "productName";
    }
}