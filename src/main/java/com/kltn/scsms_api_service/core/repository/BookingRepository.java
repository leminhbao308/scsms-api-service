package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    
    /**
     * Tìm booking theo mã booking
     */
    Optional<Booking> findByBookingCode(String bookingCode);
    
    /**
     * Tìm booking theo khách hàng
     */
    List<Booking> findByCustomer_UserIdOrderByCreatedDateDesc(UUID customerId);
    
    /**
     * Tìm booking theo chi nhánh
     */
    List<Booking> findByBranch_BranchIdOrderByScheduledStartAtDesc(UUID branchId);
    
    /**
     * Tìm booking theo bay
     */
    List<Booking> findByServiceBay_BayId(UUID bayId);
    
    /**
     * Tìm booking theo trạng thái
     */
    List<Booking> findByStatusOrderByScheduledStartAtAsc(Booking.BookingStatus status);
    
    /**
     * Tìm booking theo chi nhánh và trạng thái
     */
    List<Booking> findByBranch_BranchIdAndStatusOrderByScheduledStartAtAsc(UUID branchId, Booking.BookingStatus status);
    
    /**
     * Tìm booking theo ngày
     */
    @Query("SELECT b FROM Booking b WHERE b.branch.branchId = :branchId " +
           "AND DATE(b.scheduledStartAt) = :bookingDate " +
           "ORDER BY b.scheduledStartAt ASC")
    List<Booking> findByBranchAndDate(@Param("branchId") UUID branchId, @Param("bookingDate") LocalDate bookingDate);
    
    /**
     * Tìm booking trong khoảng thời gian
     */
    @Query("SELECT b FROM Booking b WHERE b.branch.branchId = :branchId " +
           "AND b.scheduledStartAt BETWEEN :startDateTime AND :endDateTime " +
           "ORDER BY b.scheduledStartAt ASC")
    List<Booking> findByBranchAndDateTimeRange(
        @Param("branchId") UUID branchId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * Tìm booking sắp tới của khách hàng
     */
    @Query("SELECT b FROM Booking b WHERE b.customer.userId = :customerId " +
           "AND b.scheduledStartAt >= :fromDateTime " +
           "AND b.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS') " +
           "ORDER BY b.scheduledStartAt ASC")
    List<Booking> findUpcomingBookingsByCustomer(
        @Param("customerId") UUID customerId,
        @Param("fromDateTime") LocalDateTime fromDateTime);
    
    /**
     * Tìm booking quá khứ của khách hàng
     */
    @Query("SELECT b FROM Booking b WHERE b.customer.userId = :customerId " +
           "AND b.scheduledStartAt < :toDateTime " +
           "ORDER BY b.scheduledStartAt DESC")
    List<Booking> findPastBookingsByCustomer(
        @Param("customerId") UUID customerId,
        @Param("toDateTime") LocalDateTime toDateTime);
    
    /**
     * Tìm booking theo xe
     */
    List<Booking> findByVehicle_VehicleIdOrderByScheduledStartAtDesc(UUID vehicleId);
    
    /**
     * Tìm booking theo độ ưu tiên
     */
    List<Booking> findByPriorityOrderByScheduledStartAtAsc(Booking.Priority priority);
    
    /**
     * Tìm booking theo trạng thái thanh toán
     */
    List<Booking> findByPaymentStatusOrderByCreatedDateDesc(Booking.PaymentStatus paymentStatus);
    
    /**
     * Tìm booking cần thanh toán
     */
    @Query("SELECT b FROM Booking b WHERE b.paymentStatus IN ('PENDING', 'PARTIAL') " +
           "AND b.status IN ('PENDING', 'CONFIRMED') " +
           "ORDER BY b.scheduledStartAt ASC")
    List<Booking> findBookingsNeedingPayment();
    
    /**
     * Tìm booking theo coupon
     */
    List<Booking> findByCouponCodeOrderByCreatedDateDesc(String couponCode);
    
    /**
     * Đếm booking theo chi nhánh và ngày
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.branch.branchId = :branchId " +
           "AND DATE(b.scheduledStartAt) = :bookingDate")
    long countByBranchAndDate(@Param("branchId") UUID branchId, @Param("bookingDate") LocalDate bookingDate);
    
    /**
     * Đếm booking theo trạng thái
     */
    long countByStatus(Booking.BookingStatus status);
    
    /**
     * Đếm booking theo chi nhánh và trạng thái
     */
    long countByBranch_BranchIdAndStatus(UUID branchId, Booking.BookingStatus status);
    
    /**
     * Tìm booking bị hủy trong khoảng thời gian
     */
    @Query("SELECT b FROM Booking b WHERE b.branch.branchId = :branchId " +
           "AND b.cancelledAt BETWEEN :startDateTime AND :endDateTime " +
           "ORDER BY b.cancelledAt DESC")
    List<Booking> findCancelledBookingsInTimeRange(
        @Param("branchId") UUID branchId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * Tìm booking hoàn thành trong khoảng thời gian
     */
    @Query("SELECT b FROM Booking b WHERE b.branch.branchId = :branchId " +
           "AND b.actualEndAt BETWEEN :startDateTime AND :endDateTime " +
           "AND b.status = 'COMPLETED' " +
           "ORDER BY b.actualEndAt DESC")
    List<Booking> findCompletedBookingsInTimeRange(
        @Param("branchId") UUID branchId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * Tìm booking theo nhân viên được phân công
     */
    @Query("SELECT DISTINCT b FROM Booking b JOIN b.assignments a " +
           "WHERE a.staff.userId = :staffId " +
           "AND b.scheduledStartAt BETWEEN :startDateTime AND :endDateTime " +
           "ORDER BY b.scheduledStartAt ASC")
    List<Booking> findBookingsByStaffInTimeRange(
        @Param("staffId") UUID staffId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * Tìm booking trùng thời gian
     */
    @Query("SELECT b FROM Booking b WHERE b.branch.branchId = :branchId " +
           "AND b.bookingId != :excludeBookingId " +
           "AND ((b.scheduledStartAt < :endDateTime AND b.scheduledEndAt > :startDateTime)) " +
           "AND b.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS')")
    List<Booking> findOverlappingBookings(
        @Param("branchId") UUID branchId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime,
        @Param("excludeBookingId") UUID excludeBookingId);
    
    /**
     * Tìm booking conflict với bay cụ thể trong khoảng thời gian
     */
    @Query("SELECT b FROM Booking b WHERE b.serviceBay.bayId = :bayId " +
           "AND ((b.scheduledStartAt < :endDateTime AND b.scheduledEndAt > :startDateTime)) " +
           "AND b.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS') " +
           "ORDER BY b.scheduledStartAt ASC")
    List<Booking> findConflictingBookings(
        @Param("bayId") UUID bayId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * Phân trang booking theo chi nhánh
     */
    Page<Booking> findByBranch_BranchIdOrderByScheduledStartAtDesc(UUID branchId, Pageable pageable);
    
    /**
     * Tìm booking theo tên khách hàng
     */
    @Query("SELECT b FROM Booking b WHERE b.customerName LIKE %:customerName% " +
           "ORDER BY b.scheduledStartAt DESC")
    List<Booking> findByCustomerNameContaining(@Param("customerName") String customerName);
    
    /**
     * Tìm booking theo số điện thoại
     */
    @Query("SELECT b FROM Booking b WHERE b.customerPhone LIKE %:phoneNumber% " +
           "ORDER BY b.scheduledStartAt DESC")
    List<Booking> findByCustomerPhoneContaining(@Param("phoneNumber") String phoneNumber);
    
    /**
     * Tìm booking theo biển số xe
     */
    @Query("SELECT b FROM Booking b WHERE b.vehicleLicensePlate LIKE %:licensePlate% " +
           "ORDER BY b.scheduledStartAt DESC")
    List<Booking> findByVehicleLicensePlateContaining(@Param("licensePlate") String licensePlate);
}
