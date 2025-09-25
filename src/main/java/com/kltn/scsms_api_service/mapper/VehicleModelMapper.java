package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.vehicleManagement.VehicleModelInfoDto;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.request.CreateVehicleModelRequest;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.response.VehicleModelDropdownResponse;
import com.kltn.scsms_api_service.core.entity.VehicleModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {UserMapper.class, AuditMapper.class})
public interface VehicleModelMapper {
    
    VehicleModelInfoDto toVehicleModelInfoDto(VehicleModel vehicleModel);
    
    VehicleModelDropdownResponse toVehicleModelDropdownResponse(VehicleModel vehicleModel);
    
    VehicleModel toEntity(CreateVehicleModelRequest createVehicleModelRequest);
}
