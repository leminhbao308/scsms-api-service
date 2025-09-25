package com.kltn.scsms_api_service.core.dto.vehicleManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateVehicleModelRequest {
    
    @JsonProperty("model_name")
    private String modelName;
    
    @JsonProperty("model_code")
    private String modelCode;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("brand_id")
    private UUID brandId;
    
    @JsonProperty("type_id")
    private UUID typeId;
}
