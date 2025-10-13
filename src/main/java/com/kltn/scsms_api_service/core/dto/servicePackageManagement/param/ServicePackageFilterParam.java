package com.kltn.scsms_api_service.core.dto.servicePackageManagement.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicePackageFilterParam extends BaseFilterParam<ServicePackageFilterParam> {
    
    // Basic filters
    @JsonProperty("category_id")
    private UUID categoryId;
    
    @JsonProperty("service_package_type_id")
    private UUID servicePackageTypeId;
    
    // Price filters
    @JsonProperty("min_price")
    private BigDecimal minPrice;
    
    @JsonProperty("max_price")
    private BigDecimal maxPrice;
    
    @JsonProperty("min_discount")
    private BigDecimal minDiscount;
    
    @JsonProperty("max_discount")
    private BigDecimal maxDiscount;
    
    @JsonProperty("min_savings")
    private BigDecimal minSavings;
    
    @JsonProperty("max_savings")
    private BigDecimal maxSavings;
    
    // Duration filters
    @JsonProperty("min_duration")
    private Integer minDuration;
    
    @JsonProperty("max_duration")
    private Integer maxDuration;
    
    // Validity filters
    @JsonProperty("min_validity_days")
    private Integer minValidityDays;
    
    @JsonProperty("max_validity_days")
    private Integer maxValidityDays;
    
    @JsonProperty("min_usage_count")
    private Integer minUsageCount;
    
    @JsonProperty("max_usage_count")
    private Integer maxUsageCount;
    
    // Date filters
    @JsonProperty("start_date_from")
    private LocalDate startDateFrom;
    
    @JsonProperty("start_date_to")
    private LocalDate startDateTo;
    
    @JsonProperty("end_date_from")
    private LocalDate endDateFrom;
    
    @JsonProperty("end_date_to")
    private LocalDate endDateTo;
    
    @JsonProperty("created_date_from")
    private LocalDateTime createdDateFrom;
    
    @JsonProperty("created_date_to")
    private LocalDateTime createdDateTo;
    
    @JsonProperty("modified_date_from")
    private LocalDateTime modifiedDateFrom;
    
    @JsonProperty("modified_date_to")
    private LocalDateTime modifiedDateTo;
    
    // Boolean filters
    @JsonProperty("is_limited_time")
    private Boolean isLimitedTime;
    
    @JsonProperty("is_popular")
    private Boolean isPopular;
    
    @JsonProperty("is_recommended")
    private Boolean isRecommended;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("is_currently_active")
    private Boolean isCurrentlyActive;
    
    @JsonProperty("is_expired")
    private Boolean isExpired;
    
    @JsonProperty("is_upcoming")
    private Boolean isUpcoming;
    
    // Search filters
    private String search;
    
    @JsonProperty("package_name")
    private String packageName;
    
    private String description;
    private String tag;
    
    @JsonProperty("target_vehicle_type")
    private String targetVehicleType;
    
    // Sort options
    @Builder.Default
    private String sort = "packageName";
    
    @Builder.Default
    private String direction = "ASC";
    
    @Override
    protected void standardizeSpecificFields(ServicePackageFilterParam filterParam) {
        // Standardize search
        if (filterParam.getSearch() != null) {
            filterParam.setSearch(filterParam.getSearch().trim());
        }
        if (filterParam.getPackageName() != null) {
            filterParam.setPackageName(filterParam.getPackageName().trim());
        }
        if (filterParam.getDescription() != null) {
            filterParam.setDescription(filterParam.getDescription().trim());
        }
        if (filterParam.getTag() != null) {
            filterParam.setTag(filterParam.getTag().trim());
        }
        if (filterParam.getTargetVehicleType() != null) {
            filterParam.setTargetVehicleType(filterParam.getTargetVehicleType().trim());
        }
    }
    
    @Override
    protected String getDefaultSortField() {
        return "packageName";
    }
}