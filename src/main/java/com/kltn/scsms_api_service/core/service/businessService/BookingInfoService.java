package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.bookingManagement.BookingInfoDto;
import com.kltn.scsms_api_service.core.entity.Booking;
import com.kltn.scsms_api_service.core.entity.BaySchedule;
import com.kltn.scsms_api_service.core.service.entityService.BayScheduleService;
import com.kltn.scsms_api_service.mapper.BookingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service để xử lý thông tin booking với slot information
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingInfoService {

    private final BookingMapper bookingMapper;
    private final BayScheduleService bayScheduleService;

    /**
     * Convert Booking entity to BookingInfoDto with slot information
     */
    public BookingInfoDto toBookingInfoDto(Booking booking) {
        BookingInfoDto dto = bookingMapper.toBookingInfoDto(booking);
        
        // Populate slot information if available
        if (booking.getServiceBay() != null && booking.getSlotStartTime() != null && booking.getScheduledStartAt() != null) {
            try {
                List<BaySchedule> schedules = bayScheduleService.getSchedulesByBooking(booking.getBookingId());
                
                if (!schedules.isEmpty()) {
                    // Get the first slot (main slot)
                    BaySchedule mainSlot = schedules.get(0);
                    dto.setSlotId(mainSlot.getScheduleId());
                    dto.setSlotStatus(mainSlot.getStatus().toString());
                }
            } catch (Exception e) {
                log.warn("Failed to populate slot information for booking {}: {}", 
                    booking.getBookingId(), e.getMessage());
            }
        }
        
        return dto;
    }

    /**
     * Convert list of Booking entities to list of BookingInfoDto with slot information
     */
    public List<BookingInfoDto> toBookingInfoDtoList(List<Booking> bookings) {
        return bookings.stream()
                .map(this::toBookingInfoDto)
                .collect(Collectors.toList());
    }
}
