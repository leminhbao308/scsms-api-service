package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.bookingManagement.TimeSlotDto;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.AvailableSlotsRequest;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.BookSlotRequest;
import com.kltn.scsms_api_service.core.dto.bookingManagement.request.CompleteEarlyRequest;
import com.kltn.scsms_api_service.core.entity.BaySchedule;
import com.kltn.scsms_api_service.core.entity.Booking;
import com.kltn.scsms_api_service.core.entity.ServiceBay;
import com.kltn.scsms_api_service.core.service.entityService.BayScheduleService;
import com.kltn.scsms_api_service.core.service.entityService.BookingService;
import com.kltn.scsms_api_service.core.service.entityService.ServiceBayService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingScheduleService {
    
    private final BayScheduleService bayScheduleService;
    private final ServiceBayService serviceBayService;
    private final BookingService bookingService;
    
    /**
     * Tìm các slot available cho booking
     */
    public List<TimeSlotDto> findAvailableSlots(AvailableSlotsRequest request) {
        log.info("Finding available slots for branch: {} on date: {} with duration: {} minutes", 
            request.getBranchId(), request.getDate(), request.getServiceDurationMinutes());
        
        // Tạo lịch cho tất cả bay trong chi nhánh nếu chưa có
        ensureDailyScheduleExists(request.getBranchId(), request.getDate());
        
        List<ServiceBay> availableBays;
        if (request.getBayId() != null) {
            // Chỉ tìm slot cho bay cụ thể
            ServiceBay bay = serviceBayService.getById(request.getBayId());
            availableBays = List.of(bay);
        } else {
            // Tìm slot cho tất cả bay trong chi nhánh
            availableBays = serviceBayService.findActiveBaysByBranch(request.getBranchId());
        }
        
        List<TimeSlotDto> availableSlots = new ArrayList<>();
        
        for (ServiceBay bay : availableBays) {
            // Lấy tất cả slot (cả available và booked) để hiển thị đúng trạng thái
            List<BaySchedule> allBaySlots = bayScheduleService.getBaySchedules(bay.getBayId(), request.getDate());
            
            for (BaySchedule slot : allBaySlots) {
                // Kiểm tra slot có đủ thời gian cho dịch vụ không (chỉ cho slot available)
                boolean canAccommodate = slot.getStatus() == BaySchedule.ScheduleStatus.AVAILABLE && 
                    canAccommodateService(slot, request.getServiceDurationMinutes(), request);
                
                TimeSlotDto timeSlot = TimeSlotDto.builder()
                    .bayId(bay.getBayId())
                    .bayName(bay.getBayName())
                    .bayCode(bay.getBayCode())
                    .startTime(slot.getStartTime())
                    .endTime(slot.getEndTime())
                    .estimatedEndTime(calculateEstimatedEndTime(
                        slot.getStartTime(),
                        request.getServiceDurationMinutes(),
                        0)) // Không cộng buffer vào service duration
                    .status(slot.getStatus().name())
                    .notes(slot.getNotes())
                    .isAvailable(slot.getStatus() == BaySchedule.ScheduleStatus.AVAILABLE && canAccommodate)
                    .durationMinutes(request.getServiceDurationMinutes())
                    .build();
                
                availableSlots.add(timeSlot);
            }
        }
        
        log.info("Found {} total slots (including booked slots)", availableSlots.size());
        return availableSlots;
    }
    
    /**
     * Đặt slot cho booking
     */
    @Transactional
    public void bookSlot(BookSlotRequest request) {
        log.info("Booking slot for bay: {} at {} {} for booking: {}", 
            request.getBayId(), request.getDate(), request.getStartTime(), request.getBookingId());
        
        // Validate bay exists
        ServiceBay bay = serviceBayService.getById(request.getBayId());
        
        // Validate booking exists
        Booking booking = bookingService.getById(request.getBookingId());
        
        // Kiểm tra slot có available không
        if (!bayScheduleService.isSlotAvailable(request.getBayId(), request.getDate(), request.getStartTime())) {
            throw new ClientSideException(ErrorCode.SLOT_NOT_AVAILABLE, 
                "Slot is not available for booking");
        }
        
        // Kiểm tra slot có đủ thời gian cho dịch vụ không
        BaySchedule slot = bayScheduleService.getSlot(request.getBayId(), request.getDate(), request.getStartTime());
        if (!canAccommodateService(slot, request.getServiceDurationMinutes(), null)) {
            throw new ClientSideException(ErrorCode.SLOT_INSUFFICIENT_TIME, 
                "Slot does not have enough time for the service");
        }
        
        // Tính thời gian kết thúc dự kiến (KHÔNG cộng buffer)
        LocalTime endTime = calculateEstimatedEndTime(
            request.getStartTime(), 
            request.getServiceDurationMinutes(), 
            0); // Không cộng buffer vào service duration
        
        // Tìm tất cả slot cần thiết cho dịch vụ
        List<BaySchedule> requiredSlots = findRequiredSlotsForService(
            request.getBayId(), 
            request.getDate(), 
            request.getStartTime(), 
            endTime);
        
        // Kiểm tra tất cả slot cần thiết đều available
        for (BaySchedule requiredSlot : requiredSlots) {
            if (requiredSlot.getStatus() != BaySchedule.ScheduleStatus.AVAILABLE) {
                throw new ClientSideException(ErrorCode.SLOT_NOT_AVAILABLE, 
                    "Required slot is not available: " + requiredSlot.getStartTime() + " - " + requiredSlot.getEndTime());
            }
        }
        
        // Đặt tất cả slot cần thiết
        for (BaySchedule requiredSlot : requiredSlots) {
            bayScheduleService.bookSlot(
                requiredSlot.getServiceBay().getBayId(),
                requiredSlot.getScheduleDate(),
                requiredSlot.getStartTime(),
                request.getBookingId());
        }
        
        // Cập nhật booking với thông tin slot
        updateBookingWithSlotInfo(booking, bay, request);
        
        log.info("Successfully booked slot for booking: {}", request.getBookingId());
    }
    
    /**
     * Hoàn thành dịch vụ sớm và mở slot trống
     */
    @Transactional
    public void completeEarlyAndReleaseSlots(CompleteEarlyRequest request) {
        log.info("Completing service early for booking: {} at {}", 
            request.getBookingId(), request.getActualCompletionTime());
        
        Booking booking = bookingService.getById(request.getBookingId());
        
        if (booking.getStatus() != Booking.BookingStatus.IN_PROGRESS) {
            throw new ClientSideException(ErrorCode.INVALID_BOOKING_STATUS, 
                "Only in-progress bookings can be completed early");
        }
        
        // Hoàn thành booking
        booking.completeService(request.getActualCompletionTime());
        bookingService.update(booking);
        
        // Hoàn thành slot
        if (booking.getServiceBay() != null && booking.getSlotStartTime() != null) {
            bayScheduleService.completeService(
                booking.getServiceBay().getBayId(),
                booking.getScheduledStartAt().toLocalDate(),
                booking.getSlotStartTime());
        }
        
        log.info("Successfully completed service early for booking: {}", request.getBookingId());
    }
    
    /**
     * Tạo lịch cho tất cả bay trong chi nhánh trong ngày
     */
    @Transactional
    public void generateBranchDailySchedule(UUID branchId, LocalDate date) {
        log.info("Generating daily schedule for branch: {} on date: {}", branchId, date);
        bayScheduleService.generateBranchDailySchedule(branchId, date);
    }
    
    /**
     * Lấy thống kê slot của bay trong ngày
     */
    public BaySlotStatistics getBaySlotStatistics(UUID bayId, LocalDate date) {
        log.info("Getting slot statistics for bay: {} on date: {}", bayId, date);
        
        List<BaySchedule> schedules = bayScheduleService.getBaySchedules(bayId, date);
        
        long totalSlots = schedules.size();
        long availableSlots = schedules.stream()
            .mapToLong(s -> s.getStatus() == BaySchedule.ScheduleStatus.AVAILABLE ? 1 : 0)
            .sum();
        long bookedSlots = schedules.stream()
            .mapToLong(s -> s.getStatus() == BaySchedule.ScheduleStatus.BOOKED ? 1 : 0)
            .sum();
        long inProgressSlots = schedules.stream()
            .mapToLong(s -> s.getStatus() == BaySchedule.ScheduleStatus.IN_PROGRESS ? 1 : 0)
            .sum();
        long completedSlots = schedules.stream()
            .mapToLong(s -> s.getStatus() == BaySchedule.ScheduleStatus.COMPLETED ? 1 : 0)
            .sum();
        long cancelledSlots = schedules.stream()
            .mapToLong(s -> s.getStatus() == BaySchedule.ScheduleStatus.CANCELLED ? 1 : 0)
            .sum();
        
        return BaySlotStatistics.builder()
            .bayId(bayId)
            .date(date)
            .totalSlots(totalSlots)
            .availableSlots(availableSlots)
            .bookedSlots(bookedSlots)
            .inProgressSlots(inProgressSlots)
            .completedSlots(completedSlots)
            .cancelledSlots(cancelledSlots)
            .utilizationRate(totalSlots > 0 ? (double) (bookedSlots + inProgressSlots + completedSlots) / totalSlots * 100 : 0)
            .build();
    }
    
    /**
     * Lấy các slot có thể mở rộng (hoàn thành sớm)
     */
    public List<TimeSlotDto> getExpandableSlots(UUID branchId, LocalDate date, LocalTime fromTime) {
        log.info("Getting expandable slots for branch: {} on date: {} from time: {}", branchId, date, fromTime);
        
        List<BaySchedule> expandableSlots = bayScheduleService.getExpandableSlots(branchId, date, fromTime);
        
        return expandableSlots.stream()
            .map(slot -> {
                ServiceBay bay = slot.getServiceBay();
                return TimeSlotDto.builder()
                    .bayId(bay.getBayId())
                    .bayName(bay.getBayName())
                    .bayCode(bay.getBayCode())
                    .startTime(slot.getStartTime())
                    .endTime(slot.getEndTime())
                    .estimatedEndTime(slot.getActualEndTime())
                    .status("EXPANDABLE")
                    .notes("Available due to early completion")
                    .isAvailable(true)
                    .durationMinutes(slot.getEarlyCompletionMinutes())
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    // Helper methods
    
    /**
     * Đảm bảo lịch ngày đã được tạo
     */
    private void ensureDailyScheduleExists(UUID branchId, LocalDate date) {
        List<ServiceBay> bays = serviceBayService.findActiveBaysByBranch(branchId);
        
        for (ServiceBay bay : bays) {
            List<BaySchedule> existingSchedules = bayScheduleService.getBaySchedules(bay.getBayId(), date);
            if (existingSchedules.isEmpty()) {
                bayScheduleService.generateDailySchedule(bay.getBayId(), date);
            }
        }
    }
    
    /**
     * Kiểm tra slot có thể chứa dịch vụ không (bao gồm cả slot liền kề)
     */
    private boolean canAccommodateService(BaySchedule slot, Integer serviceDurationMinutes, AvailableSlotsRequest request) {
        // Kiểm tra thời gian
        if (serviceDurationMinutes == null || serviceDurationMinutes <= 0) {
            return false;
        }
        
        // Kiểm tra filter thời gian nếu có
        if (request != null) {
            if (request.getFromHour() != null && slot.getStartTime().getHour() < request.getFromHour()) {
                return false;
            }
            if (request.getToHour() != null && slot.getStartTime().getHour() >= request.getToHour()) {
                return false;
            }
        }
        
        // Kiểm tra slot có đủ thời gian không (KHÔNG cộng buffer)
        ServiceBay bay = slot.getServiceBay();
        LocalTime estimatedEndTime = calculateEstimatedEndTime(
            slot.getStartTime(), 
            serviceDurationMinutes, 
            0); // Không cộng buffer vào service duration
        
        // Nếu dịch vụ chỉ cần 1 slot
        if (!estimatedEndTime.isAfter(slot.getEndTime())) {
            return true;
        }
        
        // Nếu dịch vụ cần nhiều slot, kiểm tra các slot liền kề
        return canAccommodateMultiSlotService(slot, serviceDurationMinutes, bay);
    }
    
    /**
     * Kiểm tra dịch vụ cần nhiều slot liền kề
     */
    private boolean canAccommodateMultiSlotService(BaySchedule startSlot, Integer serviceDurationMinutes, ServiceBay bay) {
        LocalTime estimatedEndTime = calculateEstimatedEndTime(
            startSlot.getStartTime(), 
            serviceDurationMinutes, 
            0); // Không cộng buffer vào service duration
        
        // Tìm tất cả slot cần thiết cho dịch vụ
        List<BaySchedule> requiredSlots = findRequiredSlotsForService(
            bay.getBayId(), 
            startSlot.getScheduleDate(), 
            startSlot.getStartTime(), 
            estimatedEndTime);
        
        // Kiểm tra tất cả slot cần thiết đều available
        for (BaySchedule requiredSlot : requiredSlots) {
            if (requiredSlot.getStatus() != BaySchedule.ScheduleStatus.AVAILABLE) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Tìm tất cả slot cần thiết cho dịch vụ
     */
    private List<BaySchedule> findRequiredSlotsForService(UUID bayId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<BaySchedule> requiredSlots = new ArrayList<>();
        
        // Lấy tất cả slot trong khoảng thời gian
        List<BaySchedule> allSlots = bayScheduleService.getBaySchedules(bayId, date);
        
        // Tìm các slot nằm trong khoảng thời gian cần thiết
        for (BaySchedule slot : allSlots) {
            // Slot bắt đầu trước hoặc bằng startTime và kết thúc sau startTime
            // HOẶC slot bắt đầu trước endTime và kết thúc sau hoặc bằng endTime
            if ((slot.getStartTime().isBefore(startTime) || slot.getStartTime().equals(startTime)) && 
                slot.getEndTime().isAfter(startTime)) {
                requiredSlots.add(slot);
            } else if (slot.getStartTime().isBefore(endTime) && 
                      (slot.getEndTime().isAfter(endTime) || slot.getEndTime().equals(endTime))) {
                requiredSlots.add(slot);
            }
        }
        
        return requiredSlots;
    }
    
    /**
     * Tính thời gian kết thúc dự kiến
     */
    private LocalTime calculateEstimatedEndTime(LocalTime startTime, Integer serviceDurationMinutes, Integer bufferMinutes) {
        int totalMinutes = serviceDurationMinutes + (bufferMinutes != null ? bufferMinutes : 0);
        return startTime.plusMinutes(totalMinutes);
    }
    
    /**
     * Cập nhật booking với thông tin slot
     */
    private void updateBookingWithSlotInfo(Booking booking, ServiceBay bay, BookSlotRequest request) {
        booking.setSlotStartTime(request.getStartTime());
        booking.setSlotEndTime(calculateEstimatedEndTime(
            request.getStartTime(), 
            request.getServiceDurationMinutes(), 
            bay.getBufferMinutes()));
        
        // Cập nhật scheduled times
        LocalDateTime scheduledStart = LocalDateTime.of(request.getDate(), request.getStartTime());
        LocalDateTime scheduledEnd = LocalDateTime.of(request.getDate(), booking.getSlotEndTime());
        
        booking.setScheduledStartAt(scheduledStart);
        booking.setScheduledEndAt(scheduledEnd);
        booking.setEstimatedDurationMinutes(request.getServiceDurationMinutes());
        
        bookingService.update(booking);
    }
    
    /**
     * DTO cho thống kê slot
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BaySlotStatistics {
        private UUID bayId;
        private LocalDate date;
        private long totalSlots;
        private long availableSlots;
        private long bookedSlots;
        private long inProgressSlots;
        private long completedSlots;
        private long cancelledSlots;
        private double utilizationRate;
    }
}
