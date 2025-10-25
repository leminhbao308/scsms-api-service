package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.bookingManagement.BookingInfoDto;
import com.kltn.scsms_api_service.core.dto.saleOrderManagement.SaleOrderInfoDto;
import com.kltn.scsms_api_service.core.entity.SalesOrder;
import com.kltn.scsms_api_service.core.service.businessService.BookingManagementService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
        AuditMapper.class,
        SaleOrderLineMapper.class,
        UserMapper.class,
        BranchMapper.class })
public abstract class SaleOrderMapper {

    @Autowired
    private BookingManagementService bookingManagementService;

    @Mapping(target = "branch", qualifiedByName = "toBranchInfoDto")
    @Mapping(target = "bookingInfo", ignore = true)
    public abstract SaleOrderInfoDto toSaleOrderInfoDto(SalesOrder saleOrder);

    @AfterMapping
    protected void enrichWithBookingInfo(@MappingTarget SaleOrderInfoDto dto, SalesOrder saleOrder) {
        if (saleOrder.getLines() != null && !saleOrder.getLines().isEmpty()) {
            // Check if any line has booking information
            UUID bookingId = saleOrder.getLines().stream()
                    .filter(line -> line.getOriginalBookingId() != null)
                    .map(line -> line.getOriginalBookingId())
                    .findFirst()
                    .orElse(null);

            if (bookingId != null) {
                try {
                    BookingInfoDto bookingInfo = bookingManagementService.getBookingById(bookingId);
                    dto.setBookingInfo(bookingInfo);
                } catch (Exception e) {
                    // If booking not found or error, just skip
                    // Log the error but don't fail the whole mapping
                }
            }
        }
    }
}
