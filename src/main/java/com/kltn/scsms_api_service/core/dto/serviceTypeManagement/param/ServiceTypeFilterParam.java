package com.kltn.scsms_api_service.core.dto.serviceTypeManagement.param;

import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Filter parameters for ServiceType queries
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceTypeFilterParam extends BaseFilterParam {
    
    private Boolean isActive;
    private String keyword;
    private Integer minDefaultDuration;
    private Integer maxDefaultDuration;
    
    /**
     * Standardize filter parameters
     */
    public static ServiceTypeFilterParam standardize(ServiceTypeFilterParam filterParam) {
        if (filterParam == null) {
            filterParam = new ServiceTypeFilterParam();
        }
        
        // Set default values
        if (filterParam.getPage() < 0) {
            filterParam.setPage(0);
        }
        if (filterParam.getSize() <= 0) {
            filterParam.setSize(20);
        }
        if (filterParam.getSize() > 100) {
            filterParam.setSize(100); // Limit max page size
        }
        if (filterParam.getSort() == null || filterParam.getSort().trim().isEmpty()) {
            filterParam.setSort("name");
        }
        if (filterParam.getDirection() == null || filterParam.getDirection().trim().isEmpty()) {
            filterParam.setDirection("ASC");
        }
        
        // Clean keyword
        if (filterParam.getKeyword() != null) {
            filterParam.setKeyword(filterParam.getKeyword().trim());
            if (filterParam.getKeyword().isEmpty()) {
                filterParam.setKeyword(null);
            }
        }
        
        return filterParam;
    }
}
