package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.BaySchedule;
import com.kltn.scsms_api_service.core.entity.Booking;
import com.kltn.scsms_api_service.core.entity.ServiceBay;
import com.kltn.scsms_api_service.core.repository.BayScheduleRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BayScheduleService {
    
    private final BayScheduleRepository bayScheduleRepository;
    private final ServiceBayService serviceBayService;
    private final BookingService bookingService;
    
    @Value("${app.schedule.mode:MONTHLY}")
    private String scheduleMode;
    
    @Value("${app.schedule.monthly-type:CURRENT_AND_NEXT}")
    private String monthlyType;
    
    @Value("${app.schedule.max-advance-booking-days:30}")
    private int maxAdvanceBookingDays;
    
    /**
     * Tạo lịch cho bay trong ngày
     */
    @Transactional
    public void generateDailySchedule(UUID bayId, LocalDate date) {
        log.info("Generating daily schedule for bay: {} on date: {}", bayId, date);
        
        ServiceBay bay = serviceBayService.getById(bayId);
        
        // Chỉ xóa slot AVAILABLE (chưa được đặt) để giữ lịch sử
        cleanupAvailableSlotsOnly(bayId, date);
        
        // Tạo các slot từ workingHoursStart đến workingHoursEnd
        LocalTime startTime = bay.getWorkingHoursStart();
        LocalTime endTime = bay.getWorkingHoursEnd();
        
        while (startTime.isBefore(endTime)) {
            LocalTime slotEndTime = startTime.plusHours(1);
            
            // Kiểm tra slot đã tồn tại chưa (tránh duplicate)
            if (!bayScheduleRepository.existsByServiceBayBayIdAndScheduleDateAndStartTimeAndStatus(
                bayId, date, startTime, BaySchedule.ScheduleStatus.AVAILABLE)) {
                
                BaySchedule schedule = BaySchedule.builder()
                    .serviceBay(bay)
                    .scheduleDate(date)
                    .startTime(startTime)
                    .endTime(slotEndTime)
                    .status(BaySchedule.ScheduleStatus.AVAILABLE)
                    .isActive(true)
                    .isDeleted(false)
                    .build();
                
                bayScheduleRepository.save(schedule);
                log.debug("Created slot: {} - {} for bay: {}", 
                    schedule.getStartTime(), schedule.getEndTime(), bay.getBayName());
            } else {
                log.debug("Slot already exists: {} - {} for bay: {}", 
                    startTime, slotEndTime, bay.getBayName());
            }
            
            startTime = startTime.plusHours(1);
        }
        
        log.info("Generated slots for bay: {} on date: {}", bay.getBayName(), date);
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
     * Kiểm tra và dọn dẹp duplicate slots
     */
    @Transactional
    public void cleanupDuplicateSlots(UUID bayId, LocalDate date) {
        log.info("Cleaning up duplicate slots for bay: {} on date: {}", bayId, date);
        
        List<BaySchedule> allSlots = bayScheduleRepository.findByServiceBayBayIdAndScheduleDateOrderByStartTime(bayId, date);
        
        // Group slots by startTime
        Map<LocalTime, List<BaySchedule>> slotsByTime = allSlots.stream()
            .collect(Collectors.groupingBy(BaySchedule::getStartTime));
        
        int duplicatesRemoved = 0;
        for (Map.Entry<LocalTime, List<BaySchedule>> entry : slotsByTime.entrySet()) {
            List<BaySchedule> slots = entry.getValue();
            if (slots.size() > 1) {
                log.warn("Found {} duplicate slots for bay: {} at time: {}", 
                    slots.size(), bayId, entry.getKey());
                
                // Keep the first slot (oldest), mark others as deleted
                // BaySchedule keepSlot = slots.get(0); // First slot is kept automatically
                for (int i = 1; i < slots.size(); i++) {
                    BaySchedule duplicateSlot = slots.get(i);
                    duplicateSlot.setIsDeleted(true);
                    duplicateSlot.setNotes("Duplicate slot - cleaned up");
                    bayScheduleRepository.save(duplicateSlot);
                    duplicatesRemoved++;
                    log.debug("Marked duplicate slot as deleted: {}", duplicateSlot.getScheduleId());
                }
            }
        }
        
        if (duplicatesRemoved > 0) {
            log.info("Cleaned up {} duplicate slots for bay: {} on date: {}", 
                duplicatesRemoved, bayId, date);
        }
    }
    
    /**
     * Tìm slot theo bay, ngày và giờ bắt đầu
     */
    public BaySchedule getSlot(UUID bayId, LocalDate date, LocalTime startTime) {
        // Tự động dọn dẹp duplicate slots trước khi tìm
        cleanupDuplicateSlots(bayId, date);
        
        return bayScheduleRepository.findByServiceBayBayIdAndScheduleDateAndStartTime(bayId, date, startTime)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, 
                "Slot not found for bay: " + bayId + " at " + date + " " + startTime));
    }
    
    /**
     * Block slot (đặt slot thành BOOKED)
     */
    @Transactional
    public void blockSlot(UUID bayId, LocalDate date, LocalTime startTime) {
        BaySchedule slot = getSlot(bayId, date, startTime);
        if (slot.getStatus() != BaySchedule.ScheduleStatus.AVAILABLE) {
            throw new ClientSideException(ErrorCode.SLOT_NOT_AVAILABLE, 
                "Slot is not available for blocking");
        }
        slot.setStatus(BaySchedule.ScheduleStatus.BOOKED);
        bayScheduleRepository.save(slot);
        log.info("Blocked slot for bay {} at {} {}", bayId, date, startTime);
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
    
    /**
     * Chỉ xóa slot AVAILABLE (chưa được đặt) để giữ lịch sử slot đã đặt
     */
    @Transactional
    public void cleanupAvailableSlotsOnly(UUID bayId, LocalDate date) {
        log.info("Cleaning up available slots only for bay: {} on date: {}", bayId, date);
        
        // Chỉ xóa slot AVAILABLE (chưa được đặt)
        bayScheduleRepository.deleteByServiceBayBayIdAndScheduleDateAndStatus(
            bayId, date, BaySchedule.ScheduleStatus.AVAILABLE);
        
        log.info("Cleaned up available slots for bay: {} on date: {}", bayId, date);
    }
    
    /**
     * Soft delete slot đã được đặt sau một khoảng thời gian (để giữ lịch sử)
     */
    @Transactional
    public void archiveOldBookedSlots(int daysToKeep) {
        log.info("Archiving old booked slots older than {} days", daysToKeep);
        
        LocalDate archiveDate = LocalDate.now().minusDays(daysToKeep);
        
        List<BaySchedule> oldBookedSlots = bayScheduleRepository
            .findByScheduleDateBeforeAndStatusInAndIsDeletedFalse(archiveDate, 
                Arrays.asList(
                    BaySchedule.ScheduleStatus.BOOKED, 
                    BaySchedule.ScheduleStatus.COMPLETED,
                    BaySchedule.ScheduleStatus.CANCELLED
                ));
        
        for (BaySchedule schedule : oldBookedSlots) {
            schedule.setIsDeleted(true);
            schedule.setIsActive(false);
            bayScheduleRepository.save(schedule);
            log.debug("Archived slot: {} for bay: {} on date: {}", 
                schedule.getStartTime(), schedule.getServiceBay().getBayName(), schedule.getScheduleDate());
        }
        
        log.info("Archived {} old booked slots", oldBookedSlots.size());
    }
    
    /**
     * Lấy lịch sử slot đã được đặt (bao gồm cả đã archive)
     */
    public List<BaySchedule> getSlotHistory(UUID bayId, LocalDate startDate, LocalDate endDate) {
        log.info("Getting slot history for bay: {} from {} to {}", bayId, startDate, endDate);
        
        return bayScheduleRepository.findByServiceBayBayIdAndScheduleDateBetweenAndStatusIn(
            bayId, startDate, endDate, 
            Arrays.asList(
                BaySchedule.ScheduleStatus.BOOKED, 
                BaySchedule.ScheduleStatus.COMPLETED,
                BaySchedule.ScheduleStatus.CANCELLED
            ));
    }
    
    /**
     * Kiểm tra xem có thể đặt lịch cho ngày này không (theo monthly mode)
     */
    public boolean isDateAvailableForBooking(LocalDate date) {
        LocalDate today = LocalDate.now();
        
        if ("MONTHLY".equalsIgnoreCase(scheduleMode)) {
            return isDateInMonthlyRange(date, today);
        } else {
            // Fallback to rolling window
            LocalDate maxBookingDate = today.plusDays(maxAdvanceBookingDays);
            return !date.isBefore(today) && !date.isAfter(maxBookingDate);
        }
    }
    
    /**
     * Kiểm tra ngày có trong phạm vi monthly không
     */
    private boolean isDateInMonthlyRange(LocalDate date, LocalDate today) {
        switch (monthlyType.toUpperCase()) {
            case "CURRENT_ONLY":
                // Chỉ tháng hiện tại
                LocalDate endOfCurrentMonth = today.withDayOfMonth(today.lengthOfMonth());
                return !date.isBefore(today) && !date.isAfter(endOfCurrentMonth);
                
            case "NEXT_30_DAYS":
                // 30 ngày từ đầu tháng hiện tại
                LocalDate startOfMonth = today.withDayOfMonth(1);
                LocalDate endDate = startOfMonth.plusDays(30);
                return !date.isBefore(today) && !date.isAfter(endDate);
                
            case "CURRENT_AND_NEXT":
            default:
                // Tháng hiện tại + tháng sau
                LocalDate endOfNextMonth = today.plusMonths(1).withDayOfMonth(
                    today.plusMonths(1).lengthOfMonth()
                );
                return !date.isBefore(today) && !date.isAfter(endOfNextMonth);
        }
    }
    
    /**
     * Validate ngày đặt lịch (theo monthly mode)
     */
    public void validateBookingDate(LocalDate date) {
        if (!isDateAvailableForBooking(date)) {
            LocalDate today = LocalDate.now();
            String rangeMessage = getMonthlyRangeMessage(today);
            throw new ClientSideException(ErrorCode.INVALID_BOOKING_DATE, 
                String.format("Booking date must be within allowed range: %s", rangeMessage));
        }
    }
    
    /**
     * Lấy thông báo về phạm vi ngày được phép đặt lịch
     */
    private String getMonthlyRangeMessage(LocalDate today) {
        switch (monthlyType.toUpperCase()) {
            case "CURRENT_ONLY":
                LocalDate endOfCurrentMonth = today.withDayOfMonth(today.lengthOfMonth());
                return String.format("from %s to end of current month (%s)", today, endOfCurrentMonth);
                
            case "NEXT_30_DAYS":
                LocalDate startOfMonth = today.withDayOfMonth(1);
                LocalDate endDate = startOfMonth.plusDays(30);
                return String.format("from %s to %s (30 days from start of month)", today, endDate);
                
            case "CURRENT_AND_NEXT":
            default:
                LocalDate endOfNextMonth = today.plusMonths(1).withDayOfMonth(
                    today.plusMonths(1).lengthOfMonth()
                );
                return String.format("from %s to end of next month (%s)", today, endOfNextMonth);
        }
    }
    
    /**
     * Kiểm tra xem đã có slot cho bay và ngày này chưa
     */
    public boolean hasSlotsForDate(UUID bayId, LocalDate date) {
        long slotCount = bayScheduleRepository.countByServiceBayBayIdAndScheduleDateAndIsDeletedFalse(bayId, date);
        return slotCount > 0;
    }
}
