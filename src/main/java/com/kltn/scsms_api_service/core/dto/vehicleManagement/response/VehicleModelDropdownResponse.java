package com.kltn.scsms_api_service.core.dto.vehicleManagement.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleModelDropdownResponse {
    @JsonProperty("model_id")
    private UUID modelId;
    
    @JsonProperty("model_name")
    private String modelName;
    
    @JsonProperty("model_code")
    private String modelCode;
}
