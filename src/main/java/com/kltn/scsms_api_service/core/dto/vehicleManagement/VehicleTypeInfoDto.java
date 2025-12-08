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
public class VehicleTypeInfoDto extends AuditDto {
    
    @JsonProperty("type_id")
    private UUID typeId;
    
    @JsonProperty("type_name")
    private String typeName;
    
    @JsonProperty("type_code")
    private String typeCode;
    
    @JsonProperty("description")
    private String description;
}
