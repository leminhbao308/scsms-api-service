package com.kltn.scsms_api_service.core.dto.serviceManagement.param;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("category_id")
    private UUID categoryId;
    
    @JsonProperty("service_type_id")
    private UUID serviceTypeId;
    
    @JsonProperty("required_skill_level")
    private Service.SkillLevel requiredSkillLevel;
    
    // Duration filters
    @JsonProperty("min_duration")
    private Integer minDuration;
    
    @JsonProperty("max_duration")
    private Integer maxDuration;
    
    // Price filters
    @JsonProperty("min_price")
    private BigDecimal minPrice;
    
    @JsonProperty("max_price")
    private BigDecimal maxPrice;
    
    // Boolean filters
    @JsonProperty("is_featured")
    private Boolean isFeatured;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    // Search filters
    @JsonProperty("search")
    private String search;
    
    @JsonProperty("service_name")
    private String serviceName;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("tag")
    private String tag;
    
    @JsonProperty("vehicle_type")
    private String vehicleType;
    
    @JsonProperty("required_tool")
    private String requiredTool;
    
    // Date filters
    @JsonProperty("created_date_from")
    private LocalDateTime createdDateFrom;
    
    @JsonProperty("created_date_to")
    private LocalDateTime createdDateTo;
    
    @JsonProperty("modified_date_from")
    private LocalDateTime modifiedDateFrom;
    
    @JsonProperty("modified_date_to")
    private LocalDateTime modifiedDateTo;
    
    // Sort options
    @Builder.Default
    @JsonProperty("sort")
    private String sort = "serviceName";
    
    @Builder.Default
    @JsonProperty("direction")
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