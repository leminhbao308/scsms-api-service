package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.bookingManagement.BookingInfoDto;
import com.kltn.scsms_api_service.core.entity.Booking;
import com.kltn.scsms_api_service.mapper.BookingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service để xử lý thông tin booking
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingInfoService {

    private final BookingMapper bookingMapper;

    /**
     * Convert Booking entity to BookingInfoDto
     */
    public BookingInfoDto toBookingInfoDto(Booking booking) {
        BookingInfoDto dto = bookingMapper.toBookingInfoDto(booking);
        
        // Không cần populate slot information nữa
        // Slot logic đã được loại bỏ, chỉ dùng scheduledStartAt/scheduledEndAt
        
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
