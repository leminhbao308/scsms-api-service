package com.kltn.scsms_api_service.core.dto.vehicleManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleModelInfoDto extends AuditDto {
    
    @JsonProperty("model_id")
    private UUID modelId;
    
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
