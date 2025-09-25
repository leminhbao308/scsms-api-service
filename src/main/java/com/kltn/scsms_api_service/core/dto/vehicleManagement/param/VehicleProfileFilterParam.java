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
public class VehicleProfileFilterParam extends BaseFilterParam<VehicleProfileFilterParam> {
    
    @JsonProperty("vehicle_brand_id")
    private UUID vehicleBrandId;
    
    @JsonProperty("vehicle_type_id")
    private UUID vehicleTypeId;
    
    @JsonProperty("vehicle_model_id")
    private UUID vehicleModelId;
    
    @JsonProperty("owner_id")
    private UUID ownerId;
    
    public static VehicleProfileFilterParam standardize(VehicleProfileFilterParam request) {
        return request.standardizeFilterRequest(request);
    }
    
    @Override
    protected void standardizeSpecificFields(VehicleProfileFilterParam request) {
        super.standardizeSpecificFields(request);
        
        if (request.getVehicleBrandId() != null && request.getVehicleBrandId().toString().isBlank()) {
            request.setVehicleBrandId(null);
        }
        
        if (request.getVehicleTypeId() != null && request.getVehicleTypeId().toString().isBlank()) {
            request.setVehicleTypeId(null);
        }
        
        if (request.getVehicleModelId() != null && request.getVehicleModelId().toString().isBlank()) {
            request.setVehicleModelId(null);
        }
        
        if (request.getOwnerId() != null && request.getOwnerId().toString().isBlank()) {
            request.setOwnerId(null);
        }
    }
    
    @Override
    protected String getDefaultSortField() {
        return super.getDefaultSortField();
    }
}
