package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.bookingManagement.BookingInfoDto;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.CreateBookingRequest;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.CreateBookingItemRequest;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.UpdateBookingRequest;
import com.kltn.scsms_api_service.core.entity.Booking;
import com.kltn.scsms_api_service.core.entity.BookingItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class BookingMapper {
    
    /**
     * Convert Booking entity to BookingInfoDto
     */
    @Mapping(target = "customerId", source = "customer.userId")
    @Mapping(target = "vehicleId", source = "vehicle.vehicleId")
    @Mapping(target = "branchId", source = "branch.branchId")
    @Mapping(target = "branchName", source = "branch.branchName")
    @Mapping(target = "branchCode", source = "branch.branchCode")
    @Mapping(target = "bayId", source = "serviceBay.bayId")
    @Mapping(target = "bayName", source = "serviceBay.bayName")
    @Mapping(target = "bookingItems", source = "bookingItems")
    @Mapping(target = "slotId", ignore = true) // Will be populated by custom method
    @Mapping(target = "slotDurationMinutes", source = "serviceBay.slotDurationMinutes")
    @Mapping(target = "slotStatus", ignore = true) // Will be populated by custom method
    @Mapping(target = "isActive", expression = "java(entity.isActive())")
    @Mapping(target = "isCancelled", expression = "java(entity.isCancelled())")
    @Mapping(target = "isCompleted", expression = "java(entity.isCompleted())")
    @Mapping(target = "needsPayment", expression = "java(entity.needsPayment())")
    @Mapping(target = "isFullyPaid", expression = "java(entity.isFullyPaid())")
    @Mapping(target = "totalEstimatedDuration", expression = "java(entity.getTotalEstimatedDuration())")
    @Mapping(target = "actualDurationMinutes", expression = "java(entity.getActualDurationMinutes())")
    @Mapping(target = "createdAt", source = "createdDate")
    @Mapping(target = "updatedAt", source = "modifiedDate")
    public abstract BookingInfoDto toBookingInfoDto(Booking entity);
    
    /**
     * Convert CreateBookingRequest to Booking entity
     */
    @Mapping(target = "bookingId", ignore = true)
    @Mapping(target = "bookingCode", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "serviceBay", ignore = true)
    @Mapping(target = "bookingItems", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "actualCheckInAt", ignore = true)
    @Mapping(target = "actualStartAt", ignore = true)
    @Mapping(target = "actualEndAt", ignore = true)
    @Mapping(target = "actualCompletionTime", ignore = true)
    @Mapping(target = "earlyCompletionMinutes", ignore = true)
    @Mapping(target = "slotStartTime", ignore = true)
    @Mapping(target = "slotEndTime", ignore = true)
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
    public abstract Booking toEntity(CreateBookingRequest request);
    
    /**
     * Update Booking entity from UpdateBookingRequest
     */
    @Mapping(target = "bookingId", ignore = true)
    @Mapping(target = "bookingCode", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "serviceBay", ignore = true)
    @Mapping(target = "bookingItems", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "actualCheckInAt", ignore = true)
    @Mapping(target = "actualStartAt", ignore = true)
    @Mapping(target = "actualEndAt", ignore = true)
    @Mapping(target = "actualCompletionTime", ignore = true)
    @Mapping(target = "earlyCompletionMinutes", ignore = true)
    @Mapping(target = "slotStartTime", ignore = true)
    @Mapping(target = "slotEndTime", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancelledBy", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    public abstract Booking updateEntity(@MappingTarget Booking entity, UpdateBookingRequest request);
    
    /**
     * Convert BookingItem entity to CreateBookingItemRequest
     */
    @Mapping(target = "serviceId", source = "itemId")
    public abstract CreateBookingItemRequest toCreateBookingItemRequest(BookingItem bookingItem);
    
    /**
     * Convert list of BookingItem entities to list of CreateBookingItemRequest
     */
    public abstract List<CreateBookingItemRequest> toCreateBookingItemRequestList(List<BookingItem> bookingItems);
}
