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
public class VehicleTypeDropdownResponse {
    @JsonProperty("type_id")
    private UUID typeId;
    
    @JsonProperty("type_name")
    private String typeName;
    
    @JsonProperty("type_code")
    private String typeCode;
}
