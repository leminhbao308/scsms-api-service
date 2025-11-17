package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.dto.bookingSchedule.BookingScheduleProjection;
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
        * Find booking by code with all related entities eagerly fetched
        * Prevents N+1 queries by using JOIN FETCH for:
        * - branch: branch information of the booking
        * - serviceBay: service bay information
        * - serviceBay.branch: branch information of the service bay
        * - bookingItems: items/services in the booking
        */
       @Query("SELECT DISTINCT b FROM Booking b " +
                     "LEFT JOIN FETCH b.branch " +
                     "LEFT JOIN FETCH b.serviceBay sb " +
                     "LEFT JOIN FETCH sb.branch " +
                     "LEFT JOIN FETCH b.bookingItems " +
                     "WHERE b.bookingCode = :bookingCode")
       Optional<Booking> findByBookingCodeWithDetails(@Param("bookingCode") String bookingCode);

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
        * Tìm booking theo nhiều trạng thái
        * Dùng để lấy booking cho quản lý chăm sóc xe (CHECKED_IN, IN_PROGRESS, CANCELLED, COMPLETED)
        */
       @Query("SELECT b FROM Booking b WHERE b.status IN :statuses " +
                     "ORDER BY b.scheduledStartAt DESC")
       List<Booking> findByStatusInOrderByScheduledStartAtDesc(@Param("statuses") List<Booking.BookingStatus> statuses);

       /**
        * Tìm booking theo chi nhánh và nhiều trạng thái
        */
       @Query("SELECT b FROM Booking b WHERE b.branch.branchId = :branchId " +
                     "AND b.status IN :statuses " +
                     "ORDER BY b.scheduledStartAt DESC")
       List<Booking> findByBranchAndStatusInOrderByScheduledStartAtDesc(
                     @Param("branchId") UUID branchId,
                     @Param("statuses") List<Booking.BookingStatus> statuses);

       /**
        * Tìm booking theo chi nhánh và trạng thái
        */
       List<Booking> findByBranch_BranchIdAndStatusOrderByScheduledStartAtAsc(UUID branchId,
                     Booking.BookingStatus status);

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

       /**
        * Tìm booking theo trạng thái thanh toán và trạng thái booking không phải
        * cancelled
        */
       List<Booking> findByPaymentStatusAndStatus(Booking.PaymentStatus paymentStatus,
                     Booking.BookingStatus bookingStatus);

       // ===== OPTIMIZED QUERIES TO PREVENT N+1 =====

       /**
        * Find booking by ID with all related entities eagerly fetched
        * Prevents N+1 queries by using JOIN FETCH for:
        * - branch: branch information of the booking
        * - serviceBay: service bay information
        * - serviceBay.branch: branch information of the service bay
        * - bookingItems: items/services in the booking
        * DTO)
        * This reduces queries from ~5 per booking to 1-2 queries
        */
       @Query("SELECT DISTINCT b FROM Booking b " +
                     "LEFT JOIN FETCH b.branch " +
                     "LEFT JOIN FETCH b.serviceBay sb " +
                     "LEFT JOIN FETCH sb.branch " +
                     "LEFT JOIN FETCH b.bookingItems " +
                     "WHERE b.bookingId = :bookingId")
       Optional<Booking> findByIdWithDetails(@Param("bookingId") UUID bookingId);
       
       /**
        * Projection query để lấy thông tin schedule bookings cho bay và ngày cụ thể
        * Chỉ lấy các field cần thiết để tính toán available time ranges
        */
       @Query("SELECT new com.kltn.scsms_api_service.core.dto.bookingSchedule.BookingScheduleProjection(" +
                     "b.bookingId, " +
                     "b.serviceBay.bayId, " +
                     "b.branch.branchId, " +
                     "b.scheduledStartAt, " +
                     "b.scheduledEndAt, " +
                     "b.status) " +
                     "FROM Booking b " +
                     "WHERE b.serviceBay.bayId = :bayId " +
                     "AND DATE(b.scheduledStartAt) = :date " +
                     "AND b.bookingType = 'SCHEDULED' " +
                     "AND b.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS') " +
                     "AND b.scheduledStartAt IS NOT NULL " +
                     "AND b.scheduledEndAt IS NOT NULL " +
                     "ORDER BY b.scheduledStartAt ASC")
       List<BookingScheduleProjection> findScheduleProjectionsByBayAndDate(
                     @Param("bayId") UUID bayId, 
                     @Param("date") LocalDate date);
       
       /**
        * Projection query để lấy thông tin schedule bookings cho chi nhánh và ngày cụ thể
        */
       @Query("SELECT new com.kltn.scsms_api_service.core.dto.bookingSchedule.BookingScheduleProjection(" +
                     "b.bookingId, " +
                     "b.serviceBay.bayId, " +
                     "b.branch.branchId, " +
                     "b.scheduledStartAt, " +
                     "b.scheduledEndAt, " +
                     "b.status) " +
                     "FROM Booking b " +
                     "WHERE b.branch.branchId = :branchId " +
                     "AND DATE(b.scheduledStartAt) = :date " +
                     "AND b.bookingType = 'SCHEDULED' " +
                     "AND b.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS') " +
                     "AND b.scheduledStartAt IS NOT NULL " +
                     "AND b.scheduledEndAt IS NOT NULL " +
                     "ORDER BY b.scheduledStartAt ASC")
       List<BookingScheduleProjection> findScheduleProjectionsByBranchAndDate(
                     @Param("branchId") UUID branchId, 
                     @Param("date") LocalDate date);
       
       /**
        * Tìm các WALK_IN bookings của bay trong ngày cụ thể, chưa kết thúc
        * Sắp xếp theo scheduledStartAt để tính toán position
        * Prevents N+1 queries by using JOIN FETCH for bookingItems
        */
       @Query("SELECT DISTINCT b FROM Booking b " +
                     "LEFT JOIN FETCH b.bookingItems " +
                     "WHERE b.serviceBay.bayId = :bayId " +
                     "AND DATE(b.scheduledStartAt) = :date " +
                     "AND b.bookingType = 'WALK_IN' " +
                     "AND b.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS') " +
                     "AND b.scheduledStartAt IS NOT NULL " +
                     "AND b.scheduledEndAt IS NOT NULL " +
                     "AND b.scheduledEndAt >= :currentTime " +
                     "ORDER BY b.scheduledStartAt ASC")
       List<Booking> findWalkInBookingsByBayAndDate(
                     @Param("bayId") UUID bayId,
                     @Param("date") LocalDate date,
                     @Param("currentTime") LocalDateTime currentTime);
       
       /**
        * Lấy scheduledEndAt lớn nhất của các WALK_IN bookings chưa kết thúc trong bay
        */
       @Query("SELECT MAX(b.scheduledEndAt) FROM Booking b WHERE b.serviceBay.bayId = :bayId " +
                     "AND DATE(b.scheduledStartAt) = :date " +
                     "AND b.bookingType = 'WALK_IN' " +
                     "AND b.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS') " +
                     "AND b.scheduledStartAt IS NOT NULL " +
                     "AND b.scheduledEndAt IS NOT NULL " +
                     "AND b.scheduledEndAt >= :currentTime")
       LocalDateTime findMaxScheduledEndAtForWalkInBookings(
                     @Param("bayId") UUID bayId,
                     @Param("date") LocalDate date,
                     @Param("currentTime") LocalDateTime currentTime);
}
