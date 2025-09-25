package com.kltn.scsms_api_service.core.dto.vehicleManagement.param;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleModelFilterParam extends BaseFilterParam<VehicleModelFilterParam> {
    
    @JsonProperty("brand_id")
    private UUID brandId;
    
    @JsonProperty("type_id")
    private UUID typeId;
    
    public static VehicleModelFilterParam standardize(VehicleModelFilterParam request) {
        return request.standardizeFilterRequest(request);
    }
    
    @Override
    protected void standardizeSpecificFields(VehicleModelFilterParam request) {
        super.standardizeSpecificFields(request);
        
        if (request.getBrandId() != null && request.getBrandId().toString().isBlank()) {
            request.setBrandId(null);
        }
        
        if (request.getTypeId() != null && request.getTypeId().toString().isBlank()) {
            request.setTypeId(null);
        }
    }
    
    @Override
    protected String getDefaultSortField() {
        return super.getDefaultSortField();
    }
}
