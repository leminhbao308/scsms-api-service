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
public class VehicleProfileInfoDto extends AuditDto {
    
    @JsonProperty("vehicle_id")
    private UUID vehicleId;
    
    @JsonProperty("license_plate")
    private String licensePlate;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("vehicle_brand_id")
    private UUID vehicleBrandId;
    
    @JsonProperty("vehicle_type_id")
    private UUID vehicleTypeId;
    
    @JsonProperty("vehicle_model_id")
    private UUID vehicleModelId;
    
    @JsonProperty("owner_id")
    private UUID ownerId;
    
    @JsonProperty("distance_traveled")
    private Double distanceTraveled;
}
