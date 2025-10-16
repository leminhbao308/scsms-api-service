package com.kltn.scsms_api_service.core.dto.vehicleManagement.param;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleBrandFilterParam extends BaseFilterParam<VehicleBrandFilterParam> {
    
    public static VehicleBrandFilterParam standardize(VehicleBrandFilterParam request) {
        return request.standardizeFilterRequest(request);
    }
    
    @Override
    protected void standardizeSpecificFields(VehicleBrandFilterParam request) {
        super.standardizeSpecificFields(request);
    }
    
    @Override
    protected String getDefaultSortField() {
        return super.getDefaultSortField();
    }
}
