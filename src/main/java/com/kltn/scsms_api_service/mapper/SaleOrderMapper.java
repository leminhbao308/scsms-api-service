package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.bookingManagement.BookingInfoDto;
import com.kltn.scsms_api_service.core.dto.saleOrderManagement.SaleOrderInfoDto;
import com.kltn.scsms_api_service.core.entity.SalesOrder;
import com.kltn.scsms_api_service.core.service.businessService.BookingManagementService;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
        AuditMapper.class,
        SaleOrderLineMapper.class,
        UserMapper.class,
        BranchMapper.class })
@Slf4j
public abstract class SaleOrderMapper {

    @Autowired
    private BookingManagementService bookingManagementService;

    @Mapping(target = "branch", qualifiedByName = "toBranchInfoDto")
    @Mapping(target = "bookingInfo", ignore = true)
    public abstract SaleOrderInfoDto toSaleOrderInfoDto(SalesOrder saleOrder);

    /**
     * Convert list of SalesOrder to list of DTOs
     * Optimized to batch-fetch bookings and prevent N+1 queries
     */
    public List<SaleOrderInfoDto> toSaleOrderInfoDtoList(List<SalesOrder> saleOrders) {
        if (saleOrders == null || saleOrders.isEmpty()) {
            return new ArrayList<>();
        }

        // Step 1: Map all orders to DTOs
        List<SaleOrderInfoDto> dtos = saleOrders.stream()
                .map(this::toSaleOrderInfoDto)
                .collect(Collectors.toList());

        // Step 2: Collect all unique booking IDs
        Set<UUID> bookingIds = new HashSet<>();
        for (SalesOrder order : saleOrders) {
            if (order.getLines() != null && !order.getLines().isEmpty()) {
                order.getLines().stream()
                        .filter(line -> line.getOriginalBookingId() != null)
                        .map(line -> line.getOriginalBookingId())
                        .forEach(bookingIds::add);
            }
        }

        // Step 3: Batch fetch all bookings at once (prevents N+1)
        Map<UUID, BookingInfoDto> bookingMap = new HashMap<>();
        if (!bookingIds.isEmpty()) {
            log.info("Batch fetching {} bookings to prevent N+1 queries", bookingIds.size());
            for (UUID bookingId : bookingIds) {
                try {
                    BookingInfoDto booking = bookingManagementService.getBookingById(bookingId);
                    bookingMap.put(bookingId, booking);
                } catch (Exception e) {
                    log.warn("Failed to fetch booking {}: {}", bookingId, e.getMessage());
                }
            }
        }

        // Step 4: Enrich DTOs with booking info
        for (int i = 0; i < saleOrders.size(); i++) {
            SalesOrder order = saleOrders.get(i);
            SaleOrderInfoDto dto = dtos.get(i);

            if (order.getLines() != null && !order.getLines().isEmpty()) {
                UUID bookingId = order.getLines().stream()
                        .filter(line -> line.getOriginalBookingId() != null)
                        .map(line -> line.getOriginalBookingId())
                        .findFirst()
                        .orElse(null);

                if (bookingId != null && bookingMap.containsKey(bookingId)) {
                    dto.setBookingInfo(bookingMap.get(bookingId));
                }
            }
        }

        return dtos;
    }

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
                    log.warn("Failed to fetch booking {} for order {}: {}",
                            bookingId, saleOrder.getId(), e.getMessage());
                }
            }
        }
    }
}
