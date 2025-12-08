package com.kltn.scsms_api_service.core.dto.vehicleManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleBrandInfoDto extends AuditDto {
    
    @JsonProperty("brand_id")
    private UUID brandId;
    
    @JsonProperty("brand_name")
    private String brandName;
    
    @JsonProperty("brand_code")
    private String brandCode;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("brand_logo_url")
    private String brandLogoUrl;
}
