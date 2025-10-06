package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.ServiceProcessTrackingInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.request.CreateServiceProcessTrackingRequest;
import com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.request.UpdateServiceProcessTrackingRequest;
import com.kltn.scsms_api_service.core.entity.ServiceProcessTracking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
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
    @Mapping(target = "estimatedTime", source = "serviceStep.estimatedTime")
    @Mapping(target = "isRequired", source = "serviceStep.isRequired")
    @Mapping(target = "technicianId", source = "technician.userId")
    @Mapping(target = "technicianName", source = "technician.fullName")
    @Mapping(target = "technicianCode", source = "technician.userId")
    @Mapping(target = "slotId", source = "slot.slotId")
    @Mapping(target = "slotName", source = "slot.slotDate")
    @Mapping(target = "slotCode", source = "slot.slotDate")
    @Mapping(target = "lastUpdatedBy", source = "lastUpdatedBy.userId")
    @Mapping(target = "lastUpdatedByName", source = "lastUpdatedBy.fullName")
    @Mapping(target = "createdAt", source = "createdDate")
    @Mapping(target = "modifiedAt", source = "modifiedDate")
    @Mapping(target = "efficiency", expression = "java(entity.getEfficiency())")
    @Mapping(target = "statusDisplay", expression = "java(entity.getStatus().toString())")
    @Mapping(target = "durationDisplay", expression = "java(entity.getActualDuration() != null ? entity.getActualDuration() + \" phút\" : (entity.getEstimatedDuration() != null ? entity.getEstimatedDuration() + \" phút (ước tính)\" : \"Chưa xác định\"))")
    @Mapping(target = "progressDisplay", expression = "java(entity.getProgressPercent() != null ? entity.getProgressPercent() + \"%\" : \"0%\")")
    ServiceProcessTrackingInfoDto toServiceProcessTrackingInfoDto(ServiceProcessTracking entity);

    /**
     * Convert CreateServiceProcessTrackingRequest to ServiceProcessTracking entity
     */
    @Mapping(target = "trackingId", ignore = true)
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "technician", ignore = true)
    @Mapping(target = "slot", ignore = true)
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "actualDuration", ignore = true)
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
    @Mapping(target = "technician", ignore = true)
    @Mapping(target = "slot", ignore = true)
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "actualDuration", ignore = true)
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
