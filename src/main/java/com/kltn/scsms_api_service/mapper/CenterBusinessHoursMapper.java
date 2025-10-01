package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.centerBusinessHours.CenterBusinessHoursDto;
import com.kltn.scsms_api_service.core.dto.centerBusinessHours.request.CreateCenterBusinessHoursRequest;
import com.kltn.scsms_api_service.core.dto.centerBusinessHours.request.UpdateCenterBusinessHoursRequest;
import com.kltn.scsms_api_service.core.entity.CenterBusinessHours;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CenterBusinessHoursMapper {
    
    @Mapping(target = "centerId", source = "center.centerId")
    CenterBusinessHoursDto toDto(CenterBusinessHours entity);
    
    @Mapping(target = "businessHoursId", ignore = true)
    @Mapping(target = "center", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    CenterBusinessHours toEntity(CreateCenterBusinessHoursRequest request);
    
    @Mapping(target = "businessHoursId", ignore = true)
    @Mapping(target = "center", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(UpdateCenterBusinessHoursRequest request, @MappingTarget CenterBusinessHours entity);
}
