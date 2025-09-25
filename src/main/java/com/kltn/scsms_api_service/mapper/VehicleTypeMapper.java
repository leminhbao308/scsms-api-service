package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.vehicleManagement.VehicleTypeInfoDto;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.request.CreateVehicleTypeRequest;
import com.kltn.scsms_api_service.core.dto.vehicleManagement.response.VehicleTypeDropdownResponse;
import com.kltn.scsms_api_service.core.entity.VehicleType;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {UserMapper.class, AuditMapper.class})
public interface VehicleTypeMapper {
    
    VehicleTypeInfoDto toVehicleTypeInfoDto(VehicleType vehicleType);
    
    VehicleTypeDropdownResponse toVehicleTypeDropdownResponse(VehicleType vehicleType);
    
    VehicleType toEntity(CreateVehicleTypeRequest createVehicleTypeRequest);
}
