package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.ServiceProcessTrackingInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.request.CreateServiceProcessTrackingRequest;
import com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.request.UpdateServiceProcessTrackingRequest;
import com.kltn.scsms_api_service.core.entity.ServiceProcessTracking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {AuditMapper.class}, unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface ServiceProcessTrackingMapper {

    /**
     * Convert ServiceProcessTracking entity to ServiceProcessTrackingInfoDto
     */
    @Mapping(target = "bookingId", source = "booking.bookingId")
    @Mapping(target = "bookingCode", source = "booking.bookingCode")
    @Mapping(target = "customerName", source = "booking.customerName")
    @Mapping(target = "customerPhone", source = "booking.customerPhone")
    @Mapping(target = "vehicleLicensePlate", source = "booking.vehicleLicensePlate")
    @Mapping(target = "serviceStepId", source = "serviceStep.id")
    @Mapping(target = "serviceStepName", source = "serviceStep.name")
    @Mapping(target = "serviceStepDescription", source = "serviceStep.description")
    @Mapping(target = "serviceStepOrder", source = "serviceStep.stepOrder")
    @Mapping(target = "isRequired", source = "serviceStep.isRequired")
    // Technician fields removed - will use bay's assigned technicians
    @Mapping(target = "bayId", source = "bay.bayId")
    @Mapping(target = "bayName", source = "bay.bayName")
    @Mapping(target = "bayCode", source = "bay.bayCode")
    @Mapping(target = "carServiceId", source = "carServiceId")
    @Mapping(target = "carServiceName", ignore = true) // Will be populated by custom method
    @Mapping(target = "lastUpdatedBy", source = "lastUpdatedBy.userId")
    @Mapping(target = "lastUpdatedByName", source = "lastUpdatedBy.fullName")
    @Mapping(target = "createdAt", source = "createdDate")
    @Mapping(target = "modifiedAt", source = "modifiedDate")
    // Efficiency calculation removed - simplified tracking
    ServiceProcessTrackingInfoDto toServiceProcessTrackingInfoDto(ServiceProcessTracking entity);   

    /**
     * Convert CreateServiceProcessTrackingRequest to ServiceProcessTracking entity
     */
    @Mapping(target = "trackingId", ignore = true)
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "bay", ignore = true)
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "lastUpdatedBy", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "serviceStep", ignore = true)
    ServiceProcessTracking toEntity(CreateServiceProcessTrackingRequest request);

    /**
     * Update ServiceProcessTracking entity from UpdateServiceProcessTrackingRequest
     */
    @Mapping(target = "trackingId", ignore = true)
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "bay", ignore = true)
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "lastUpdatedBy", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "serviceStep", ignore = true)
    ServiceProcessTracking updateEntity(@MappingTarget ServiceProcessTracking entity, UpdateServiceProcessTrackingRequest request);
}
