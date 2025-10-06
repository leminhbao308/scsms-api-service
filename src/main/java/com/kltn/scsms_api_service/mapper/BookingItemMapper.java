package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.bookingManagement.BookingItemInfoDto;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.CreateBookingItemRequest;
import com.kltn.scsms_api_service.core.entity.BookingItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BookingItemMapper {
    
    /**
     * Convert BookingItem entity to BookingItemInfoDto
     */
    @Mapping(target = "bookingId", source = "booking.bookingId")
    @Mapping(target = "subtotalAmount", expression = "java(entity.getSubtotalAmount())")
    @Mapping(target = "actualDurationMinutes", expression = "java(entity.getActualDurationMinutes())")
    @Mapping(target = "isCompleted", expression = "java(entity.isCompleted())")
    @Mapping(target = "isInProgress", expression = "java(entity.isInProgress())")
    @Mapping(target = "createdAt", source = "createdDate")
    @Mapping(target = "updatedAt", source = "modifiedDate")
    BookingItemInfoDto toBookingItemInfoDto(BookingItem entity);
    
    /**
     * Convert CreateBookingItemRequest to BookingItem entity
     */
    @Mapping(target = "bookingItemId", ignore = true)
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "itemStatus", ignore = true)
    @Mapping(target = "actualStartAt", ignore = true)
    @Mapping(target = "actualEndAt", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    BookingItem toEntity(CreateBookingItemRequest request);
}
