package com.kltn.scsms_api_service.core.dto.servicePackageManagement.param;

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
    private UUID categoryId;
    private UUID servicePackageTypeId;
    
    // Price filters
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minDiscount;
    private BigDecimal maxDiscount;
    private BigDecimal minSavings;
    private BigDecimal maxSavings;
    
    // Duration filters
    private Integer minDuration;
    private Integer maxDuration;
    
    // Validity filters
    private Integer minValidityDays;
    private Integer maxValidityDays;
    private Integer minUsageCount;
    private Integer maxUsageCount;
    
    // Date filters
    private LocalDate startDateFrom;
    private LocalDate startDateTo;
    private LocalDate endDateFrom;
    private LocalDate endDateTo;
    private LocalDateTime createdDateFrom;
    private LocalDateTime createdDateTo;
    private LocalDateTime modifiedDateFrom;
    private LocalDateTime modifiedDateTo;
    
    // Boolean filters
    private Boolean isLimitedTime;
    private Boolean isPopular;
    private Boolean isRecommended;
    private Boolean isActive;
    private Boolean isCurrentlyActive;
    private Boolean isExpired;
    private Boolean isUpcoming;
    
    // Search filters
    private String search;
    private String packageName;
    private String description;
    private String tag;
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