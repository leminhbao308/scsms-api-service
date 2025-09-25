package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.vehicleManagement.VehicleBrandInfoDto;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.request.CreateVehicleBrandRequest;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.response.VehicleBrandDropdownResponse;
import com.kltn.scsms_api_service.core.entity.VehicleBrand;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {UserMapper.class, AuditMapper.class})
public interface VehicleBrandMapper {
    
    VehicleBrandInfoDto toVehicleBrandInfoDto(VehicleBrand vehicleBrand);
    
    VehicleBrandDropdownResponse toVehicleBrandDropdownResponse(VehicleBrand vehicleBrand);
    
    VehicleBrand toEntity(VehicleBrandInfoDto vehicleBrandInfoDto);
    
    VehicleBrand toEntity(CreateVehicleBrandRequest createVehicleBrandRequest);
}
