package com.kltn.scsms_api_service.core.dto.vehicleManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateVehicleBrandRequest {
    
    @JsonProperty("brand_name")
    private String brandName;
    
    @JsonProperty("brand_code")
    private String brandCode;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("brand_logo_url")
    private String brandLogoUrl;
}
