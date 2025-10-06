package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.bookingManagement.BookingAssignmentInfoDto;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.CreateBookingAssignmentRequest;
import com.kltn.scsms_api_service.core.entity.BookingAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BookingAssignmentMapper {
    
    /**
     * Convert BookingAssignment entity to BookingAssignmentInfoDto
     */
    @Mapping(target = "bookingId", source = "booking.bookingId")
    @Mapping(target = "staffId", source = "staff.userId")
    @Mapping(target = "staffName", source = "staff.fullName")
    @Mapping(target = "staffCode", source = "staff.userId")
    @Mapping(target = "staffPhone", source = "staff.phoneNumber")
    @Mapping(target = "staffEmail", source = "staff.email")
    @Mapping(target = "actualDurationMinutes", expression = "java(entity.getActualDurationMinutes())")
    @Mapping(target = "estimatedDurationMinutes", expression = "java(entity.getEstimatedDurationMinutes())")
    @Mapping(target = "isActive", expression = "java(entity.isActive())")
    @Mapping(target = "isCompleted", expression = "java(entity.isCompleted())")
    @Mapping(target = "createdAt", source = "createdDate")
    @Mapping(target = "updatedAt", source = "modifiedDate")
    BookingAssignmentInfoDto toBookingAssignmentInfoDto(BookingAssignment entity);
    
    /**
     * Convert CreateBookingAssignmentRequest to BookingAssignment entity
     */
    @Mapping(target = "assignmentId", ignore = true)
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "staff", ignore = true)
    @Mapping(target = "assignmentStatus", ignore = true)
    @Mapping(target = "actualStartAt", ignore = true)
    @Mapping(target = "actualEndAt", ignore = true)
    @Mapping(target = "assignedAt", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    BookingAssignment toEntity(CreateBookingAssignmentRequest request);
}
