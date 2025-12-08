package com.kltn.scsms_api_service.core.dto.vehicleManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateVehicleTypeRequest {
    
    @JsonProperty("type_name")
    private String typeName;
    
    @JsonProperty("type_code")
    private String typeCode;
    
    @JsonProperty("description")
    private String description;
}
