package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.bookingManagement.BookingInfoDto;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.CreateBookingRequest;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.UpdateBookingRequest;
import com.kltn.scsms_api_service.core.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BookingMapper {
    
    /**
     * Convert Booking entity to BookingInfoDto
     */
    @Mapping(target = "customerId", source = "customer.userId")
    @Mapping(target = "vehicleId", source = "vehicle.vehicleId")
    @Mapping(target = "branchId", source = "branch.branchId")
    @Mapping(target = "branchName", source = "branch.branchName")
    @Mapping(target = "branchCode", source = "branch.branchCode")
    @Mapping(target = "slotId", source = "serviceSlot.slotId")
    @Mapping(target = "slotCategory", source = "serviceSlot.slotCategory")
    @Mapping(target = "isActive", expression = "java(entity.isActive())")
    @Mapping(target = "isCancelled", expression = "java(entity.isCancelled())")
    @Mapping(target = "isCompleted", expression = "java(entity.isCompleted())")
    @Mapping(target = "needsPayment", expression = "java(entity.needsPayment())")
    @Mapping(target = "isFullyPaid", expression = "java(entity.isFullyPaid())")
    @Mapping(target = "totalEstimatedDuration", expression = "java(entity.getTotalEstimatedDuration())")
    @Mapping(target = "actualDurationMinutes", expression = "java(entity.getActualDurationMinutes())")
    @Mapping(target = "createdAt", source = "createdDate")
    @Mapping(target = "updatedAt", source = "modifiedDate")
    BookingInfoDto toBookingInfoDto(Booking entity);
    
    /**
     * Convert CreateBookingRequest to Booking entity
     */
    @Mapping(target = "bookingId", ignore = true)
    @Mapping(target = "bookingCode", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "serviceSlot", ignore = true)
    @Mapping(target = "bookingItems", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "actualCheckInAt", ignore = true)
    @Mapping(target = "actualStartAt", ignore = true)
    @Mapping(target = "actualEndAt", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancelledBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Booking toEntity(CreateBookingRequest request);
    
    /**
     * Update Booking entity from UpdateBookingRequest
     */
    @Mapping(target = "bookingId", ignore = true)
    @Mapping(target = "bookingCode", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "serviceSlot", ignore = true)
    @Mapping(target = "bookingItems", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "actualCheckInAt", ignore = true)
    @Mapping(target = "actualStartAt", ignore = true)
    @Mapping(target = "actualEndAt", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancelledBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Booking updateEntity(@MappingTarget Booking entity, UpdateBookingRequest request);
}
