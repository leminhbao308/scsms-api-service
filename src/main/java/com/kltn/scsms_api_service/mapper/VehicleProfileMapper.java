package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.vehicleManagement.VehicleProfileInfoDto;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.request.CreateVehicleProfileRequest;
import com.kltn.scsms_api_service.core.entity.VehicleProfile;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        UserMapper.class,
        VehicleBrandMapper.class,
        VehicleTypeMapper.class,
        VehicleModelMapper.class,
        AuditMapper.class})
public interface VehicleProfileMapper {
    
    VehicleProfileInfoDto toVehicleProfileInfoDto(VehicleProfile vehicleProfile);
    
    VehicleProfile toEntity(CreateVehicleProfileRequest createVehicleModelRequest);
}
