package com.kltn.scsms_api_service.core.dto.servicePackageTypeManagement.param;

import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Filter parameters for ServicePackageType queries
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ServicePackageTypeFilterParam extends BaseFilterParam {
    
    private Boolean isActive;
    private Boolean isDefault;
    private String customerType;
    private String keyword;
    
    /**
     * Standardize filter parameters
     */
    public static ServicePackageTypeFilterParam standardize(ServicePackageTypeFilterParam filterParam) {
        if (filterParam == null) {
            filterParam = new ServicePackageTypeFilterParam();
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
        
        // Clean customer type
        if (filterParam.getCustomerType() != null) {
            filterParam.setCustomerType(filterParam.getCustomerType().trim());
            if (filterParam.getCustomerType().isEmpty()) {
                filterParam.setCustomerType(null);
            }
        }
        
        return filterParam;
    }
}
