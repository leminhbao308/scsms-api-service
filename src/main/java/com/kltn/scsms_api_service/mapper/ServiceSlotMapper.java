package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.serviceSlotManagement.ServiceSlotInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceSlotManagement.request.CreateServiceSlotRequest;
import com.kltn.scsms_api_service.core.dto.serviceSlotManagement.request.UpdateServiceSlotRequest;
import com.kltn.scsms_api_service.core.entity.ServiceSlot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ServiceSlotMapper {
    
    /**
     * Convert ServiceSlot entity to ServiceSlotInfoDto
     */
    @Mapping(target = "branchId", source = "branch.branchId")
    @Mapping(target = "branchName", source = "branch.branchName")
    @Mapping(target = "branchCode", source = "branch.branchCode")
    @Mapping(target = "durationInMinutes", expression = "java(entity.getDurationInMinutes())")
    @Mapping(target = "isAvailable", expression = "java(entity.isAvailable())")
    @Mapping(target = "isVipSlot", expression = "java(entity.isVipSlot())")
    @Mapping(target = "isMaintenanceSlot", expression = "java(entity.isMaintenanceSlot())")
    ServiceSlotInfoDto toServiceSlotInfoDto(ServiceSlot entity);
    
    /**
     * Convert CreateServiceSlotRequest to ServiceSlot entity
     */
    @Mapping(target = "slotId", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "currentBooking", ignore = true)
    ServiceSlot toEntity(CreateServiceSlotRequest request);
    
    /**
     * Update ServiceSlot entity from UpdateServiceSlotRequest
     */
    @Mapping(target = "slotId", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "currentBooking", ignore = true)
    ServiceSlot updateEntity(@MappingTarget ServiceSlot entity, UpdateServiceSlotRequest request);
}
