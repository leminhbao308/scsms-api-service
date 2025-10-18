package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.BaySchedule;
import com.kltn.scsms_api_service.core.entity.Booking;
import com.kltn.scsms_api_service.core.entity.ServiceBay;
import com.kltn.scsms_api_service.core.repository.BayScheduleRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BayScheduleService {
    
    private final BayScheduleRepository bayScheduleRepository;
    private final ServiceBayService serviceBayService;
    private final BookingService bookingService;
    
    /**
     * Tạo lịch cho bay trong ngày
     */
    @Transactional
    public void generateDailySchedule(UUID bayId, LocalDate date) {
        log.info("Generating daily schedule for bay: {} on date: {}", bayId, date);
        
        ServiceBay bay = serviceBayService.getById(bayId);
        
        // Xóa lịch cũ nếu có
        bayScheduleRepository.deleteByServiceBayBayIdAndScheduleDate(bayId, date);
        
        // Tạo các slot từ workingHoursStart đến workingHoursEnd
        for (int hour = bay.getWorkingHoursStart(); hour < bay.getWorkingHoursEnd(); hour++) {
            BaySchedule schedule = BaySchedule.builder()
                .serviceBay(bay)
                .scheduleDate(date)
                .startTime(LocalTime.of(hour, 0))
                .endTime(LocalTime.of(hour + 1, 0))
                .status(BaySchedule.ScheduleStatus.AVAILABLE)
                .isActive(true)
                .isDeleted(false)
                .build();
            
            bayScheduleRepository.save(schedule);
            log.debug("Created slot: {} - {} for bay: {}", 
                schedule.getStartTime(), schedule.getEndTime(), bay.getBayName());
        }
        
        log.info("Generated {} slots for bay: {} on date: {}", 
            bay.getTotalSlotsPerDay(), bay.getBayName(), date);
    }
    
    /**
     * Tạo lịch cho tất cả bay trong chi nhánh trong ngày
     */
    @Transactional
    public void generateBranchDailySchedule(UUID branchId, LocalDate date) {
        log.info("Generating daily schedule for branch: {} on date: {}", branchId, date);
        
        List<ServiceBay> bays = serviceBayService.findActiveBaysByBranch(branchId);
        
        for (ServiceBay bay : bays) {
            generateDailySchedule(bay.getBayId(), date);
        }
        
        log.info("Generated daily schedule for {} bays in branch: {} on date: {}", 
            bays.size(), branchId, date);
    }
    
    /**
     * Lấy tất cả schedule của bay trong ngày
     */
    public List<BaySchedule> getBaySchedules(UUID bayId, LocalDate date) {
        return bayScheduleRepository.findByServiceBayBayIdAndScheduleDateOrderByStartTime(bayId, date);
    }
    
    /**
     * Lấy các slot available của bay trong ngày
     */
    public List<BaySchedule> getAvailableSlots(UUID bayId, LocalDate date) {
        return bayScheduleRepository.findByServiceBayBayIdAndScheduleDateAndStatusOrderByStartTime(
            bayId, date, BaySchedule.ScheduleStatus.AVAILABLE);
    }
    
    /**
     * Lấy các slot available của tất cả bay trong chi nhánh trong ngày
     */
    public List<BaySchedule> getAvailableSlotsByBranch(UUID branchId, LocalDate date) {
        return bayScheduleRepository.findByBranchIdAndDateAndStatus(
            branchId, date, BaySchedule.ScheduleStatus.AVAILABLE);
    }
    
    /**
     * Kiểm tra slot có available không
     */
    public boolean isSlotAvailable(UUID bayId, LocalDate date, LocalTime startTime) {
        return bayScheduleRepository.existsByServiceBayBayIdAndScheduleDateAndStartTimeAndStatus(
            bayId, date, startTime, BaySchedule.ScheduleStatus.AVAILABLE);
    }
    
    /**
     * Tìm slot theo bay, ngày và giờ bắt đầu
     */
    public BaySchedule getSlot(UUID bayId, LocalDate date, LocalTime startTime) {
        return bayScheduleRepository.findByServiceBayBayIdAndScheduleDateAndStartTime(bayId, date, startTime)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, 
                "Slot not found for bay: " + bayId + " at " + date + " " + startTime));
    }
    
    /**
     * Đặt slot cho booking
     */
    @Transactional
    public void bookSlot(UUID bayId, LocalDate date, LocalTime startTime, UUID bookingId) {
        log.info("Booking slot for bay: {} at {} {} for booking: {}", bayId, date, startTime, bookingId);
        
        BaySchedule schedule = getSlot(bayId, date, startTime);
        Booking booking = bookingService.getById(bookingId);
        
        if (!schedule.isAvailable()) {
            throw new ClientSideException(ErrorCode.SLOT_NOT_AVAILABLE, 
                "Slot is not available for booking");
        }
        
        schedule.bookSlot(booking);
        bayScheduleRepository.save(schedule);
        
        log.info("Successfully booked slot for booking: {}", bookingId);
    }
    
    /**
     * Bắt đầu dịch vụ cho slot
     */
    @Transactional
    public void startService(UUID bayId, LocalDate date, LocalTime startTime) {
        log.info("Starting service for bay: {} at {} {}", bayId, date, startTime);
        
        BaySchedule schedule = getSlot(bayId, date, startTime);
        
        if (!schedule.isBooked()) {
            throw new ClientSideException(ErrorCode.INVALID_SLOT_STATUS, 
                "Cannot start service for slot that is not booked");
        }
        
        schedule.startService();
        bayScheduleRepository.save(schedule);
        
        log.info("Successfully started service for slot");
    }
    
    /**
     * Hoàn thành dịch vụ cho slot
     */
    @Transactional
    public void completeService(UUID bayId, LocalDate date, LocalTime startTime) {
        log.info("Completing service for bay: {} at {} {}", bayId, date, startTime);
        
        BaySchedule schedule = getSlot(bayId, date, startTime);
        
        if (!schedule.isInProgress()) {
            throw new ClientSideException(ErrorCode.INVALID_SLOT_STATUS, 
                "Cannot complete service for slot that is not in progress");
        }
        
        schedule.completeService();
        bayScheduleRepository.save(schedule);
        
        // Nếu hoàn thành sớm, mở các slot trống
        if (schedule.isCompletedEarly()) {
            releaseSlotsDueToEarlyCompletion(bayId, date, schedule.getActualEndTime(), schedule.getEndTime());
        }
        
        log.info("Successfully completed service for slot");
    }
    
    /**
     * Hủy slot
     */
    @Transactional
    public void cancelSlot(UUID bayId, LocalDate date, LocalTime startTime, String reason) {
        log.info("Cancelling slot for bay: {} at {} {} with reason: {}", bayId, date, startTime, reason);
        
        BaySchedule schedule = getSlot(bayId, date, startTime);
        schedule.cancelSlot(reason);
        bayScheduleRepository.save(schedule);
        
        log.info("Successfully cancelled slot");
    }
    
    /**
     * Giải phóng slot (trở về available)
     */
    @Transactional
    public void releaseSlot(UUID bayId, LocalDate date, LocalTime startTime) {
        log.info("Releasing slot for bay: {} at {} {}", bayId, date, startTime);
        
        BaySchedule schedule = getSlot(bayId, date, startTime);
        schedule.releaseSlot();
        bayScheduleRepository.save(schedule);
        
        log.info("Successfully released slot");
    }
    
    /**
     * Mở các slot trống do hoàn thành sớm
     */
    @Transactional
    public void releaseSlotsDueToEarlyCompletion(UUID bayId, LocalDate date, 
                                               LocalTime actualEndTime, LocalTime originalEndTime) {
        log.info("Releasing slots due to early completion for bay: {} from {} to {}", 
            bayId, actualEndTime, originalEndTime);
        
        // Tìm các slot trong khoảng thời gian trống
        List<BaySchedule> slotsToRelease = bayScheduleRepository.findSlotsInTimeRange(
            bayId, date, actualEndTime, originalEndTime);
        
        for (BaySchedule slot : slotsToRelease) {
            if (slot.getStatus() == BaySchedule.ScheduleStatus.BOOKED) {
                slot.releaseSlot();
                bayScheduleRepository.save(slot);
                log.debug("Released slot: {} - {} due to early completion", 
                    slot.getStartTime(), slot.getEndTime());
            }
        }
        
        log.info("Released {} slots due to early completion", slotsToRelease.size());
    }
    
    /**
     * Block các slot trong khoảng thời gian (khi đặt booking)
     */
    @Transactional
    public void blockSlotsInTimeRange(UUID bayId, LocalDate date, 
                                    LocalTime startTime, LocalTime endTime) {
        log.info("Blocking slots for bay: {} from {} to {} on {}", bayId, startTime, endTime, date);
        
        List<BaySchedule> slotsToBlock = bayScheduleRepository.findSlotsInTimeRange(
            bayId, date, startTime, endTime);
        
        for (BaySchedule slot : slotsToBlock) {
            if (slot.getStatus() == BaySchedule.ScheduleStatus.AVAILABLE) {
                slot.setStatus(BaySchedule.ScheduleStatus.BOOKED);
                bayScheduleRepository.save(slot);
                log.debug("Blocked slot: {} - {}", slot.getStartTime(), slot.getEndTime());
            }
        }
        
        log.info("Blocked {} slots in time range", slotsToBlock.size());
    }
    
    /**
     * Kiểm tra conflict trong khoảng thời gian
     */
    public List<BaySchedule> findConflictingSlots(UUID bayId, LocalDate date, 
                                                LocalTime startTime, LocalTime endTime) {
        return bayScheduleRepository.findConflictingSlots(bayId, date, startTime, endTime);
    }
    
    /**
     * Lấy schedule theo booking
     */
    public List<BaySchedule> getSchedulesByBooking(UUID bookingId) {
        return bayScheduleRepository.findByBookingBookingId(bookingId);
    }
    
    /**
     * Đếm số slot available của bay trong ngày
     */
    public long countAvailableSlots(UUID bayId, LocalDate date) {
        return bayScheduleRepository.countByServiceBayBayIdAndScheduleDateAndStatus(
            bayId, date, BaySchedule.ScheduleStatus.AVAILABLE);
    }
    
    /**
     * Lấy các slot hoàn thành sớm trong ngày
     */
    public List<BaySchedule> getEarlyCompletedSlots(UUID bayId, LocalDate date) {
        return bayScheduleRepository.findEarlyCompletedSlots(bayId, date);
    }
    
    /**
     * Lấy các slot có thể mở rộng (hoàn thành sớm)
     */
    public List<BaySchedule> getExpandableSlots(UUID branchId, LocalDate date, LocalTime fromTime) {
        return bayScheduleRepository.findExpandableSlots(branchId, date, fromTime);
    }
    
    /**
     * Lưu schedule
     */
    @Transactional
    public BaySchedule save(BaySchedule schedule) {
        return bayScheduleRepository.save(schedule);
    }
    
    /**
     * Cập nhật schedule
     */
    @Transactional
    public BaySchedule update(BaySchedule schedule) {
        return bayScheduleRepository.save(schedule);
    }
    
    /**
     * Xóa schedule
     */
    @Transactional
    public void delete(UUID scheduleId) {
        bayScheduleRepository.deleteById(scheduleId);
    }
    
    /**
     * Lấy schedule theo ID
     */
    public BaySchedule getById(UUID scheduleId) {
        return bayScheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, 
                "BaySchedule not found with ID: " + scheduleId));
    }
}
