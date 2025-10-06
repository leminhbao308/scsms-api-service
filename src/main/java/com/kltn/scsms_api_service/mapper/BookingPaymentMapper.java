package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.bookingManagement.BookingPaymentInfoDto;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.CreateBookingPaymentRequest;
import com.kltn.scsms_api_service.core.entity.BookingPayment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BookingPaymentMapper {
    
    /**
     * Convert BookingPayment entity to BookingPaymentInfoDto
     */
    @Mapping(target = "bookingId", source = "booking.bookingId")
    @Mapping(target = "totalAmount", expression = "java(entity.getTotalAmount())")
    @Mapping(target = "isSuccessful", expression = "java(entity.isSuccessful())")
    @Mapping(target = "isFailed", expression = "java(entity.isFailed())")
    @Mapping(target = "isPending", expression = "java(entity.isPending())")
    @Mapping(target = "isRefunded", expression = "java(entity.isRefunded())")
    @Mapping(target = "createdAt", source = "createdDate")
    @Mapping(target = "updatedAt", source = "modifiedDate")
    BookingPaymentInfoDto toBookingPaymentInfoDto(BookingPayment entity);
    
    /**
     * Convert CreateBookingPaymentRequest to BookingPayment entity
     */
    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "refundAmount", ignore = true)
    @Mapping(target = "refundReason", ignore = true)
    @Mapping(target = "refundedAt", ignore = true)
    @Mapping(target = "refundedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    BookingPayment toEntity(CreateBookingPaymentRequest request);
}
