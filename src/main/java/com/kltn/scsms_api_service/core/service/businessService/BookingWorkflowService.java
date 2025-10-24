package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.entity.BaySchedule;
import com.kltn.scsms_api_service.core.entity.Booking;
import com.kltn.scsms_api_service.core.service.entityService.BayScheduleService;
import com.kltn.scsms_api_service.core.service.entityService.BookingService;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingWorkflowService {
    
    private final BookingService bookingService;
    private final BayScheduleService bayScheduleService;
    
    /**
     * Workflow: PENDING → CONFIRMED → CHECKED_IN → IN_PROGRESS → COMPLETED
     */
    
    /**
     * Xác nhận booking (PENDING → CONFIRMED)
     */
    @Transactional
    public void confirmBooking(UUID bookingId) {
        log.info("Confirming booking: {}", bookingId);
        
        Booking booking = bookingService.getById(bookingId);
        
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new ClientSideException(ErrorCode.INVALID_BOOKING_STATUS, 
                "Only pending bookings can be confirmed");
        }
        
        // Validate slot information
        if (booking.getServiceBay() == null || booking.getSlotStartTime() == null) {
            throw new ClientSideException(ErrorCode.MISSING_SLOT_INFO, 
                "Booking must have slot information before confirmation");
        }
        
        // Kiểm tra slot đã được book chưa (tránh double booking)
        if (!bayScheduleService.isSlotAvailable(
            booking.getServiceBay().getBayId(),
            booking.getScheduledStartAt().toLocalDate(),
            booking.getSlotStartTime())) {
            
            // Kiểm tra slot có thuộc về booking này không
            List<BaySchedule> existingSlots = bayScheduleService.getSchedulesByBooking(bookingId);
            boolean slotBelongsToBooking = existingSlots.stream()
                .anyMatch(slot -> slot.getStartTime().equals(booking.getSlotStartTime()));
            
            if (!slotBelongsToBooking) {
                throw new ClientSideException(ErrorCode.SLOT_NOT_AVAILABLE, 
                    "Slot is not available for booking confirmation");
            }
        } else {
            // Chuyển slot từ AVAILABLE sang BOOKED
            bayScheduleService.bookSlot(
                booking.getServiceBay().getBayId(),
                booking.getScheduledStartAt().toLocalDate(),
                booking.getSlotStartTime(),
                bookingId);
        }
        
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        bookingService.update(booking);
        
        log.info("Successfully confirmed booking: {}", bookingId);
    }
    
    /**
     * Check-in booking (CONFIRMED → CHECKED_IN)
     */
    @Transactional
    public void checkInBooking(UUID bookingId) {
        log.info("Checking in booking: {}", bookingId);
        
        Booking booking = bookingService.getById(bookingId);
        
        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new ClientSideException(ErrorCode.INVALID_BOOKING_STATUS, 
                "Only confirmed bookings can be checked in");
        }
        
        // Chuyển slot sang IN_PROGRESS
        bayScheduleService.startService(
            booking.getServiceBay().getBayId(),
            booking.getScheduledStartAt().toLocalDate(),
            booking.getSlotStartTime());
        
        booking.setStatus(Booking.BookingStatus.CHECKED_IN);
        booking.setActualCheckInAt(LocalDateTime.now());
        bookingService.update(booking);
        
        log.info("Successfully checked in booking: {}", bookingId);
    }
    
    /**
     * Bắt đầu dịch vụ (CHECKED_IN → IN_PROGRESS)
     */
    @Transactional
    public void startService(UUID bookingId) {
        log.info("Starting service for booking: {}", bookingId);
        
        Booking booking = bookingService.getById(bookingId);
        
        if (booking.getStatus() != Booking.BookingStatus.CHECKED_IN) {
            throw new ClientSideException(ErrorCode.INVALID_BOOKING_STATUS, 
                "Only checked-in bookings can be started");
        }
        
        booking.setStatus(Booking.BookingStatus.IN_PROGRESS);
        booking.setActualStartAt(LocalDateTime.now());
        bookingService.update(booking);
        
        log.info("Successfully started service for booking: {}", bookingId);
    }
    
    /**
     * Hoàn thành dịch vụ (IN_PROGRESS → COMPLETED)
     */
    @Transactional
    public void completeService(UUID bookingId) {
        log.info("Completing service for booking: {}", bookingId);
        
        Booking booking = bookingService.getById(bookingId);
        
        if (booking.getStatus() != Booking.BookingStatus.IN_PROGRESS) {
            throw new ClientSideException(ErrorCode.INVALID_BOOKING_STATUS, 
                "Only in-progress bookings can be completed");
        }
        
        // Hoàn thành booking
        booking.completeService();
        bookingService.update(booking);
        
        // Hoàn thành TẤT CẢ slot của booking và xử lý early completion
        bayScheduleService.completeAllSlotsForBooking(bookingId);
        
        log.info("Successfully completed service for booking: {}", bookingId);
    }
    
    /**
     * Hoàn thành dịch vụ với thời gian cụ thể
     */
    @Transactional
    public void completeService(UUID bookingId, LocalDateTime completionTime) {
        log.info("Completing service for booking: {} at {}", bookingId, completionTime);
        
        Booking booking = bookingService.getById(bookingId);
        
        if (booking.getStatus() != Booking.BookingStatus.IN_PROGRESS) {
            throw new ClientSideException(ErrorCode.INVALID_BOOKING_STATUS, 
                "Only in-progress bookings can be completed");
        }
        
        // Hoàn thành booking với thời gian cụ thể
        booking.completeService(completionTime);
        bookingService.update(booking);
        
        // Hoàn thành TẤT CẢ slot của booking và xử lý early completion
        bayScheduleService.completeAllSlotsForBooking(bookingId, completionTime);
        
        log.info("Successfully completed service for booking: {} at {}", bookingId, completionTime);
    }
    
    /**
     * Hủy booking
     */
    @Transactional
    public void cancelBooking(UUID bookingId, String reason, String cancelledBy) {
        log.info("Cancelling booking: {} with reason: {}", bookingId, reason);
        
        Booking booking = bookingService.getById(bookingId);
        
        if (booking.isCompleted() || booking.isCancelled()) {
            throw new ClientSideException(ErrorCode.INVALID_BOOKING_STATUS, 
                "Cannot cancel completed or already cancelled booking");
        }
        
        // Giải phóng tất cả slot liên quan đến booking (để slot có thể được sử dụng lại)
        if (booking.getServiceBay() != null && booking.getSlotStartTime() != null) {
            bayScheduleService.releaseAllSlotsForBooking(booking.getBookingId());
        }
        
        // Hủy booking
        booking.cancelBooking(reason, cancelledBy);
        bookingService.update(booking);
        
        log.info("Successfully cancelled booking: {}", bookingId);
    }
    
    /**
     * Tạm dừng dịch vụ (IN_PROGRESS → PAUSED)
     */
    @Transactional
    public void pauseService(UUID bookingId, String reason) {
        log.info("Pausing service for booking: {} with reason: {}", bookingId, reason);
        
        Booking booking = bookingService.getById(bookingId);
        
        if (booking.getStatus() != Booking.BookingStatus.IN_PROGRESS) {
            throw new ClientSideException(ErrorCode.INVALID_BOOKING_STATUS, 
                "Only in-progress bookings can be paused");
        }
        
        booking.setStatus(Booking.BookingStatus.PAUSED);
        if (reason != null) {
            booking.setNotes((booking.getNotes() != null ? booking.getNotes() + "\n" : "") + "Paused: " + reason);
        }
        bookingService.update(booking);
        
        log.info("Successfully paused service for booking: {}", bookingId);
    }
    
    /**
     * Tiếp tục dịch vụ (PAUSED → IN_PROGRESS)
     */
    @Transactional
    public void resumeService(UUID bookingId) {
        log.info("Resuming service for booking: {}", bookingId);
        
        Booking booking = bookingService.getById(bookingId);
        
        if (booking.getStatus() != Booking.BookingStatus.PAUSED) {
            throw new ClientSideException(ErrorCode.INVALID_BOOKING_STATUS, 
                "Only paused bookings can be resumed");
        }
        
        booking.setStatus(Booking.BookingStatus.IN_PROGRESS);
        bookingService.update(booking);
        
        log.info("Successfully resumed service for booking: {}", bookingId);
    }
    
    /**
     * Kiểm tra booking có thể chuyển trạng thái không
     */
    public boolean canTransitionTo(Booking.BookingStatus currentStatus, Booking.BookingStatus targetStatus) {
        switch (currentStatus) {
            case PENDING:
                return targetStatus == Booking.BookingStatus.CONFIRMED || 
                       targetStatus == Booking.BookingStatus.CANCELLED;
            case CONFIRMED:
                return targetStatus == Booking.BookingStatus.CHECKED_IN || 
                       targetStatus == Booking.BookingStatus.CANCELLED;
            case CHECKED_IN:
                return targetStatus == Booking.BookingStatus.IN_PROGRESS || 
                       targetStatus == Booking.BookingStatus.CANCELLED;
            case IN_PROGRESS:
                return targetStatus == Booking.BookingStatus.COMPLETED || 
                       targetStatus == Booking.BookingStatus.PAUSED ||
                       targetStatus == Booking.BookingStatus.CANCELLED;
            case PAUSED:
                return targetStatus == Booking.BookingStatus.IN_PROGRESS || 
                       targetStatus == Booking.BookingStatus.CANCELLED;
            case COMPLETED:
            case CANCELLED:
            case NO_SHOW:
                return false; // Terminal states
            default:
                return false;
        }
    }
    
    /**
     * Lấy trạng thái tiếp theo có thể chuyển đến
     */
    public Booking.BookingStatus[] getNextPossibleStatuses(Booking.BookingStatus currentStatus) {
        switch (currentStatus) {
            case PENDING:
                return new Booking.BookingStatus[]{Booking.BookingStatus.CONFIRMED, Booking.BookingStatus.CANCELLED};
            case CONFIRMED:
                return new Booking.BookingStatus[]{Booking.BookingStatus.CHECKED_IN, Booking.BookingStatus.CANCELLED};
            case CHECKED_IN:
                return new Booking.BookingStatus[]{Booking.BookingStatus.IN_PROGRESS, Booking.BookingStatus.CANCELLED};
            case IN_PROGRESS:
                return new Booking.BookingStatus[]{Booking.BookingStatus.COMPLETED, Booking.BookingStatus.PAUSED, Booking.BookingStatus.CANCELLED};
            case PAUSED:
                return new Booking.BookingStatus[]{Booking.BookingStatus.IN_PROGRESS, Booking.BookingStatus.CANCELLED};
            default:
                return new Booking.BookingStatus[0];
        }
    }
}
