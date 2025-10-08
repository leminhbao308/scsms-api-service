package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.serviceBayManagement.ServiceBayInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceBayManagement.request.CreateServiceBayRequest;
import com.kltn.scsms_api_service.core.dto.serviceBayManagement.request.UpdateServiceBayRequest;
import com.kltn.scsms_api_service.core.entity.ServiceBay;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper cho ServiceBay entity và DTOs
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServiceBayMapper {
    
    /**
     * Chuyển đổi từ ServiceBay entity sang ServiceBayInfoDto
     */
    @Mapping(target = "branchName", source = "branch.branchName")
    @Mapping(target = "branchCode", source = "branch.branchCode")
    @Mapping(target = "isAvailable", expression = "java(entity.isActive())")
    @Mapping(target = "isMaintenance", expression = "java(entity.isMaintenance())")
    @Mapping(target = "isClosed", expression = "java(entity.isClosed())")
    @Mapping(target = "isWashBay", expression = "java(entity.isWashBay())")
    @Mapping(target = "isRepairBay", expression = "java(entity.isRepairBay())")
    @Mapping(target = "isLiftBay", expression = "java(entity.isLiftBay())")
    @Mapping(target = "totalBookings", expression = "java(entity.getBookings() != null ? (long) entity.getBookings().size() : 0L)")
    @Mapping(target = "activeBookings", expression = "java(entity.getBookings() != null ? entity.getBookings().stream().filter(booking -> booking.isActive()).count() : 0L)")
    ServiceBayInfoDto toServiceBayInfoDto(ServiceBay entity);
    
    /**
     * Chuyển đổi từ CreateServiceBayRequest sang ServiceBay entity
     */
    @Mapping(target = "bayId", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "version", constant = "0L")
    ServiceBay toEntity(CreateServiceBayRequest request);
    
    /**
     * Cập nhật ServiceBay entity từ UpdateServiceBayRequest
     */
    @Mapping(target = "bayId", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(@MappingTarget ServiceBay entity, UpdateServiceBayRequest request);
    
    /**
     * Chuyển đổi danh sách ServiceBay sang danh sách ServiceBayInfoDto
     */
    List<ServiceBayInfoDto> toServiceBayInfoDtoList(List<ServiceBay> entities);
}
