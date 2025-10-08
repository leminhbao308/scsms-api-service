package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.Booking;
import com.kltn.scsms_api_service.core.repository.BookingRepository;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingService {
    
    private final BookingRepository bookingRepository;
    
    public Booking getById(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Booking not found with ID: " + bookingId));
    }
    
    public Booking getByBookingCode(String bookingCode) {
        return bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Booking not found with code: " + bookingCode));
    }
    
    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }
    
    public Page<Booking> findAll(Pageable pageable) {
        return bookingRepository.findAll(pageable);
    }
    
    @Transactional
    public Booking save(Booking booking) {
        return bookingRepository.save(booking);
    }
    
    @Transactional
    public Booking update(Booking booking) {
        return bookingRepository.save(booking);
    }
    
    @Transactional
    public void delete(UUID bookingId) {
        Booking booking = getById(bookingId);
        booking.setIsDeleted(true);
        bookingRepository.save(booking);
    }
    
    public List<Booking> findByCustomer(UUID customerId) {
        return bookingRepository.findByCustomer_UserIdOrderByCreatedDateDesc(customerId);
    }
    
    public List<Booking> findByBranch(UUID branchId) {
        return bookingRepository.findByBranch_BranchIdOrderByScheduledStartAtDesc(branchId);
    }
    
    public Page<Booking> findByBranch(UUID branchId, Pageable pageable) {
        return bookingRepository.findByBranch_BranchIdOrderByScheduledStartAtDesc(branchId, pageable);
    }
    
    public List<Booking> findByBranchAndDate(UUID branchId, LocalDate bookingDate) {
        return bookingRepository.findByBranchAndDate(branchId, bookingDate);
    }
    
    public List<Booking> findByBranchAndDateTimeRange(UUID branchId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return bookingRepository.findByBranchAndDateTimeRange(branchId, startDateTime, endDateTime);
    }
    
    public List<Booking> findByStatus(Booking.BookingStatus status) {
        return bookingRepository.findByStatusOrderByScheduledStartAtAsc(status);
    }
    
    public List<Booking> findByBranchAndStatus(UUID branchId, Booking.BookingStatus status) {
        return bookingRepository.findByBranch_BranchIdAndStatusOrderByScheduledStartAtAsc(branchId, status);
    }
    
    public List<Booking> findUpcomingBookingsByCustomer(UUID customerId, LocalDateTime fromDateTime) {
        return bookingRepository.findUpcomingBookingsByCustomer(customerId, fromDateTime);
    }
    
    public List<Booking> findPastBookingsByCustomer(UUID customerId, LocalDateTime toDateTime) {
        return bookingRepository.findPastBookingsByCustomer(customerId, toDateTime);
    }
    
    public List<Booking> findByVehicle(UUID vehicleId) {
        return bookingRepository.findByVehicle_VehicleIdOrderByScheduledStartAtDesc(vehicleId);
    }
    
    public List<Booking> findByPriority(Booking.Priority priority) {
        return bookingRepository.findByPriorityOrderByScheduledStartAtAsc(priority);
    }
    
    public List<Booking> findByPaymentStatus(Booking.PaymentStatus paymentStatus) {
        return bookingRepository.findByPaymentStatusOrderByCreatedDateDesc(paymentStatus);
    }
    
    public List<Booking> findBookingsNeedingPayment() {
        return bookingRepository.findBookingsNeedingPayment();
    }
    
    public List<Booking> findByCouponCode(String couponCode) {
        return bookingRepository.findByCouponCodeOrderByCreatedDateDesc(couponCode);
    }
    
    public long countByBranchAndDate(UUID branchId, LocalDate bookingDate) {
        return bookingRepository.countByBranchAndDate(branchId, bookingDate);
    }
    
    public long countByStatus(Booking.BookingStatus status) {
        return bookingRepository.countByStatus(status);
    }
    
    public long countByBranchAndStatus(UUID branchId, Booking.BookingStatus status) {
        return bookingRepository.countByBranch_BranchIdAndStatus(branchId, status);
    }
    
    public List<Booking> findCancelledBookingsInTimeRange(UUID branchId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return bookingRepository.findCancelledBookingsInTimeRange(branchId, startDateTime, endDateTime);
    }
    
    public List<Booking> findCompletedBookingsInTimeRange(UUID branchId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return bookingRepository.findCompletedBookingsInTimeRange(branchId, startDateTime, endDateTime);
    }
    
    public List<Booking> findBookingsByStaffInTimeRange(UUID staffId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return bookingRepository.findBookingsByStaffInTimeRange(staffId, startDateTime, endDateTime);
    }
    
    public List<Booking> findOverlappingBookings(UUID branchId, LocalDateTime startDateTime, LocalDateTime endDateTime, UUID excludeBookingId) {
        return bookingRepository.findOverlappingBookings(branchId, startDateTime, endDateTime, excludeBookingId);
    }
    
    public List<Booking> findByCustomerNameContaining(String customerName) {
        return bookingRepository.findByCustomerNameContaining(customerName);
    }
    
    public List<Booking> findByCustomerPhoneContaining(String phoneNumber) {
        return bookingRepository.findByCustomerPhoneContaining(phoneNumber);
    }
    
    public List<Booking> findByVehicleLicensePlateContaining(String licensePlate) {
        return bookingRepository.findByVehicleLicensePlateContaining(licensePlate);
    }
    
    public boolean existsByBookingCode(String bookingCode) {
        return bookingRepository.findByBookingCode(bookingCode).isPresent();
    }
    
    public boolean existsByBay(UUID bayId) {
        return !bookingRepository.findByServiceBay_BayId(bayId).isEmpty();
    }
    
    public List<Booking> findByServiceBay(UUID bayId) {
        return bookingRepository.findByServiceBay_BayId(bayId);
    }
    
    /**
     * Tìm các booking conflict với bay và thời gian cụ thể
     */
    public List<Booking> findConflictingBookings(UUID bayId, LocalDateTime startTime, LocalDateTime endTime) {
        return bookingRepository.findConflictingBookings(bayId, startTime, endTime);
    }
}
