package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.bookingSchedule.AvailableTimeRangesResponse;
import com.kltn.scsms_api_service.core.dto.bookingSchedule.BookingScheduleProjection;
import com.kltn.scsms_api_service.core.dto.bookingSchedule.TimeRangeDto;
import com.kltn.scsms_api_service.core.dto.bookingSchedule.WorkingHoursDto;
import com.kltn.scsms_api_service.core.entity.Branch;
import com.kltn.scsms_api_service.core.entity.Center;
import com.kltn.scsms_api_service.core.entity.CenterBusinessHours;
import com.kltn.scsms_api_service.core.entity.ServiceBay;
import com.kltn.scsms_api_service.core.repository.BookingRepository;
import com.kltn.scsms_api_service.core.service.entityService.BranchService;
import com.kltn.scsms_api_service.core.service.entityService.CenterBusinessHoursService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceBayService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service tính toán available time ranges từ bookings thực tế
 * Không tạo slot trước, tính toán động dựa trên bookings
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingTimeRangeService {
    
    private final BookingRepository bookingRepository;
    private final BranchService branchService;
    private final CenterBusinessHoursService centerBusinessHoursService;
    private final ServiceBayService serviceBayService;
    
    /**
     * Lấy available time ranges cho bay và ngày cụ thể
     */
    public AvailableTimeRangesResponse getAvailableTimeRanges(UUID bayId, LocalDate date) {
        log.info("Getting available time ranges for bay: {} on date: {}", bayId, date);
        
        // 1. Lấy thông tin bay
        ServiceBay bay = serviceBayService.getById(bayId);
        
        // 2. Lấy working hours từ CenterBusinessHours
        WorkingHoursDto workingHours = getWorkingHours(bay.getBranch().getBranchId(), date);
        
        // 3. Query bookings trong ngày (dùng projection query)
        List<BookingScheduleProjection> bookings = bookingRepository
            .findScheduleProjectionsByBayAndDate(bayId, date);
        
        // 4. Tính available time ranges
        List<TimeRangeDto> availableRanges = calculateAvailableTimeRanges(
            workingHours.getStart(),
            workingHours.getEnd(),
            bookings
        );
        
        // 5. Build response
        return AvailableTimeRangesResponse.builder()
            .date(date)
            .bayId(bayId)
            .bayName(bay.getBayName())
            .workingHours(workingHours)
            .availableTimeRanges(availableRanges)
            .build();
    }
    
    /**
     * Lấy working hours từ CenterBusinessHours (theo dayOfWeek)
     * Không lấy từ ServiceBay
     */
    private WorkingHoursDto getWorkingHours(UUID branchId, LocalDate date) {
        // 1. Lấy Branch
        Branch branch = branchService.findById(branchId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, 
                "Branch not found with ID: " + branchId));
        
        // 2. Lấy Center từ Branch
        Center center = branch.getCenter();
        if (center == null) {
            throw new ClientSideException(ErrorCode.NOT_FOUND, 
                "Center not found for branch: " + branchId);
        }
        
        // 3. Tính dayOfWeek từ date
        String dayOfWeek = date.getDayOfWeek().name(); // MONDAY, TUESDAY, ...
        
        // 4. Query CenterBusinessHours
        CenterBusinessHours businessHours = centerBusinessHoursService
            .findByCenterAndDayOfWeek(center.getCenterId(), dayOfWeek)
            .orElseThrow(() -> new ClientSideException(ErrorCode.BUSINESS_HOURS_NOT_FOUND, 
                "Business hours not configured for " + dayOfWeek));
        
        // 5. Kiểm tra ngày nghỉ
        if (Boolean.TRUE.equals(businessHours.getIsClosed())) {
            throw new ClientSideException(ErrorCode.BRANCH_CLOSED, 
                "Branch is closed on " + dayOfWeek);
        }
        
        // 6. Validate openTime và closeTime
        if (businessHours.getOpenTime() == null || businessHours.getCloseTime() == null) {
            throw new ClientSideException(ErrorCode.BUSINESS_HOURS_NOT_FOUND, 
                "Business hours not properly configured for " + dayOfWeek);
        }
        
        return WorkingHoursDto.builder()
            .start(businessHours.getOpenTime())
            .end(businessHours.getCloseTime())
            .build();
    }
    
    /**
     * Tính các khoảng thời gian trống từ bookings
     */
    private List<TimeRangeDto> calculateAvailableTimeRanges(
        LocalTime workingStart,
        LocalTime workingEnd,
        List<BookingScheduleProjection> bookings
    ) {
        List<TimeRangeDto> ranges = new ArrayList<>();
        
        // Nếu chưa có booking nào, trả về toàn bộ working hours
        if (bookings == null || bookings.isEmpty()) {
            ranges.add(TimeRangeDto.builder()
                .startTime(workingStart)
                .endTime(workingEnd)
                .build());
            return ranges;
        }
        
        // Sắp xếp bookings theo scheduledStartAt
        List<BookingScheduleProjection> sortedBookings = bookings.stream()
            .sorted(Comparator.comparing(BookingScheduleProjection::getScheduledStartAt))
            .collect(Collectors.toList());
        
        LocalTime currentStart = workingStart;
        
        for (BookingScheduleProjection booking : sortedBookings) {
            LocalTime bookingStart = booking.getScheduledStartAt().toLocalTime();
            LocalTime bookingEnd = booking.getScheduledEndAt().toLocalTime();
            
            // Thêm buffer time (1 giây) để tránh overlap
            // Booking kết thúc lẻ (14:20) sẽ được làm tròn lên (14:21)
            bookingEnd = bookingEnd.plusSeconds(1);
            
            // Nếu có khoảng trống trước booking
            if (currentStart.isBefore(bookingStart)) {
                // Kết thúc khoảng trống = bookingStart - 1 giây
                LocalTime rangeEnd = bookingStart.minusSeconds(1);
                ranges.add(TimeRangeDto.builder()
                    .startTime(currentStart)
                    .endTime(rangeEnd)
                    .build());
            }
            
            // Cập nhật currentStart = bookingEnd
            currentStart = bookingEnd;
        }
        
        // Khoảng trống cuối cùng (sau booking cuối cùng)
        if (currentStart.isBefore(workingEnd) || currentStart.equals(workingEnd)) {
            ranges.add(TimeRangeDto.builder()
                .startTime(currentStart)
                .endTime(workingEnd)
                .build());
        }
        
        return ranges;
    }
}

