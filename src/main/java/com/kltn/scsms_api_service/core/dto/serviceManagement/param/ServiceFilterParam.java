package com.kltn.scsms_api_service.core.dto.serviceManagement.param;

import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import com.kltn.scsms_api_service.core.entity.Service;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceFilterParam extends BaseFilterParam<ServiceFilterParam> {
    
    // Basic filters
    private UUID categoryId;
    private Service.ServiceType serviceType;
    private Service.SkillLevel requiredSkillLevel;
    
    // Duration filters
    private Integer minDuration;
    private Integer maxDuration;
    
    // Price filters
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    
    // Boolean filters
    private Boolean isPackage;
    private Boolean isFeatured;
    private Boolean isActive;
    private Boolean photoRequired;
    
    // Search filters
    private String search;
    private String serviceName;
    private String description;
    private String tag;
    private String vehicleType;
    private String requiredTool;
    
    // Date filters
    private LocalDateTime createdDateFrom;
    private LocalDateTime createdDateTo;
    private LocalDateTime modifiedDateFrom;
    private LocalDateTime modifiedDateTo;
    
    // Sort options
    @Builder.Default
    private String sort = "serviceName";
    @Builder.Default
    private String direction = "ASC";
    
    @Override
    protected void standardizeSpecificFields(ServiceFilterParam filterParam) {
        // Standardize search
        if (filterParam.getSearch() != null) {
            filterParam.setSearch(filterParam.getSearch().trim());
        }
        if (filterParam.getServiceName() != null) {
            filterParam.setServiceName(filterParam.getServiceName().trim());
        }
        if (filterParam.getDescription() != null) {
            filterParam.setDescription(filterParam.getDescription().trim());
        }
        if (filterParam.getTag() != null) {
            filterParam.setTag(filterParam.getTag().trim());
        }
        if (filterParam.getVehicleType() != null) {
            filterParam.setVehicleType(filterParam.getVehicleType().trim());
        }
        if (filterParam.getRequiredTool() != null) {
            filterParam.setRequiredTool(filterParam.getRequiredTool().trim());
        }
    }
    
    @Override
    protected String getDefaultSortField() {
        return "serviceName";
    }
}