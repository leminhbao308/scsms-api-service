package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.BaySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BayScheduleRepository extends JpaRepository<BaySchedule, UUID> {
    
    /**
     * Tìm tất cả schedule của bay trong ngày (chỉ lấy slot chưa bị xóa)
     */
    @Query("SELECT bs FROM BaySchedule bs " +
           "WHERE bs.serviceBay.bayId = :bayId " +
           "AND bs.scheduleDate = :date " +
           "AND bs.isDeleted = false " +
           "ORDER BY bs.startTime ASC")
    List<BaySchedule> findByServiceBayBayIdAndScheduleDateOrderByStartTime(
        @Param("bayId") UUID bayId, 
        @Param("date") LocalDate date);
    
    /**
     * Tìm schedule theo bay, ngày và giờ bắt đầu (chỉ lấy slot chưa bị xóa)
     */
    @Query("SELECT bs FROM BaySchedule bs " +
           "WHERE bs.serviceBay.bayId = :bayId " +
           "AND bs.scheduleDate = :date " +
           "AND bs.startTime = :startTime " +
           "AND bs.isDeleted = false " +
           "ORDER BY bs.createdDate ASC")
    Optional<BaySchedule> findByServiceBayBayIdAndScheduleDateAndStartTime(
        @Param("bayId") UUID bayId, 
        @Param("date") LocalDate date, 
        @Param("startTime") LocalTime startTime);
    
    /**
     * Tìm các slot available của bay trong ngày (chỉ lấy slot chưa bị xóa)
     */
    @Query("SELECT bs FROM BaySchedule bs " +
           "WHERE bs.serviceBay.bayId = :bayId " +
           "AND bs.scheduleDate = :date " +
           "AND bs.status = :status " +
           "AND bs.isDeleted = false " +
           "ORDER BY bs.startTime ASC")
    List<BaySchedule> findByServiceBayBayIdAndScheduleDateAndStatusOrderByStartTime(
        @Param("bayId") UUID bayId, 
        @Param("date") LocalDate date, 
        @Param("status") BaySchedule.ScheduleStatus status);
    
    /**
     * Tìm các slot available của tất cả bay trong chi nhánh trong ngày (chỉ lấy slot chưa bị xóa)
     */
    @Query("SELECT bs FROM BaySchedule bs " +
           "JOIN bs.serviceBay sb " +
           "WHERE sb.branch.branchId = :branchId " +
           "AND bs.scheduleDate = :date " +
           "AND bs.status = :status " +
           "AND bs.isDeleted = false " +
           "ORDER BY sb.displayOrder, bs.startTime")
    List<BaySchedule> findByBranchIdAndDateAndStatus(
        @Param("branchId") UUID branchId, 
        @Param("date") LocalDate date, 
        @Param("status") BaySchedule.ScheduleStatus status);
    
    /**
     * Kiểm tra slot có available không (chỉ kiểm tra slot chưa bị xóa)
     */
    @Query("SELECT COUNT(bs) > 0 FROM BaySchedule bs " +
           "WHERE bs.serviceBay.bayId = :bayId " +
           "AND bs.scheduleDate = :date " +
           "AND bs.startTime = :startTime " +
           "AND bs.status = :status " +
           "AND bs.isDeleted = false")
    boolean existsByServiceBayBayIdAndScheduleDateAndStartTimeAndStatus(
        @Param("bayId") UUID bayId, 
        @Param("date") LocalDate date, 
        @Param("startTime") LocalTime startTime, 
        @Param("status") BaySchedule.ScheduleStatus status);
    
    /**
     * Tìm các slot bị conflict trong khoảng thời gian (chỉ lấy slot chưa bị xóa)
     */
    @Query("SELECT bs FROM BaySchedule bs " +
           "WHERE bs.serviceBay.bayId = :bayId " +
           "AND bs.scheduleDate = :date " +
           "AND bs.status IN ('BOOKED', 'IN_PROGRESS') " +
           "AND bs.isDeleted = false " +
           "AND ((bs.startTime < :endTime AND bs.endTime > :startTime))")
    List<BaySchedule> findConflictingSlots(
        @Param("bayId") UUID bayId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime);
    
    /**
     * Tìm các slot trong khoảng thời gian (chỉ lấy slot chưa bị xóa)
     */
    @Query("SELECT bs FROM BaySchedule bs " +
           "WHERE bs.serviceBay.bayId = :bayId " +
           "AND bs.scheduleDate = :date " +
           "AND bs.startTime >= :startTime " +
           "AND bs.endTime <= :endTime " +
           "AND bs.isDeleted = false " +
           "ORDER BY bs.startTime")
    List<BaySchedule> findSlotsInTimeRange(
        @Param("bayId") UUID bayId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime);
    
    /**
     * Tìm schedule theo booking (chỉ lấy slot chưa bị xóa)
     */
    @Query("SELECT bs FROM BaySchedule bs " +
           "WHERE bs.booking.bookingId = :bookingId " +
           "AND bs.isDeleted = false " +
           "ORDER BY bs.startTime ASC")
    List<BaySchedule> findByBookingBookingId(@Param("bookingId") UUID bookingId);
    
    /**
     * Đếm số slot available của bay trong ngày (chỉ đếm slot chưa bị xóa)
     */
    @Query("SELECT COUNT(bs) FROM BaySchedule bs " +
           "WHERE bs.serviceBay.bayId = :bayId " +
           "AND bs.scheduleDate = :date " +
           "AND bs.status = :status " +
           "AND bs.isDeleted = false")
    long countByServiceBayBayIdAndScheduleDateAndStatus(
        @Param("bayId") UUID bayId, 
        @Param("date") LocalDate date, 
        @Param("status") BaySchedule.ScheduleStatus status);
    
    /**
     * Tìm các slot hoàn thành sớm trong ngày (chỉ lấy slot chưa bị xóa)
     */
    @Query("SELECT bs FROM BaySchedule bs " +
           "WHERE bs.serviceBay.bayId = :bayId " +
           "AND bs.scheduleDate = :date " +
           "AND bs.status = 'COMPLETED' " +
           "AND bs.actualEndTime < bs.endTime " +
           "AND bs.isDeleted = false " +
           "ORDER BY bs.actualEndTime")
    List<BaySchedule> findEarlyCompletedSlots(
        @Param("bayId") UUID bayId,
        @Param("date") LocalDate date);
    
    /**
     * Xóa tất cả schedule của bay trong ngày (dùng khi tạo lại lịch)
     */
    void deleteByServiceBayBayIdAndScheduleDate(UUID bayId, LocalDate date);
    
    /**
     * Tìm các slot có thể mở rộng (hoàn thành sớm) (chỉ lấy slot chưa bị xóa)
     */
    @Query("SELECT bs FROM BaySchedule bs " +
           "WHERE bs.serviceBay.branch.branchId = :branchId " +
           "AND bs.scheduleDate = :date " +
           "AND bs.status = 'COMPLETED' " +
           "AND bs.actualEndTime < bs.endTime " +
           "AND bs.actualEndTime >= :fromTime " +
           "AND bs.isDeleted = false " +
           "ORDER BY bs.actualEndTime")
    List<BaySchedule> findExpandableSlots(
        @Param("branchId") UUID branchId,
        @Param("date") LocalDate date,
        @Param("fromTime") LocalTime fromTime);
    
    /**
     * Xóa slot AVAILABLE của bay trong ngày (chỉ xóa slot chưa được đặt và chưa bị xóa)
     */
    @Modifying
    @Query("DELETE FROM BaySchedule bs " +
           "WHERE bs.serviceBay.bayId = :bayId " +
           "AND bs.scheduleDate = :date " +
           "AND bs.status = :status " +
           "AND bs.isDeleted = false")
    void deleteByServiceBayBayIdAndScheduleDateAndStatus(
        @Param("bayId") UUID bayId, 
        @Param("date") LocalDate date, 
        @Param("status") BaySchedule.ScheduleStatus status);
    
    /**
     * Tìm slot đã được đặt trước ngày cụ thể (để archive)
     */
    List<BaySchedule> findByScheduleDateBeforeAndStatusInAndIsDeletedFalse(
        LocalDate date, List<BaySchedule.ScheduleStatus> statuses);
    
    /**
     * Tìm lịch sử slot trong khoảng thời gian (bao gồm cả đã archive)
     */
    @Query("SELECT bs FROM BaySchedule bs " +
           "WHERE bs.serviceBay.bayId = :bayId " +
           "AND bs.scheduleDate BETWEEN :startDate AND :endDate " +
           "AND bs.status IN :statuses " +
           "ORDER BY bs.scheduleDate, bs.startTime")
    List<BaySchedule> findByServiceBayBayIdAndScheduleDateBetweenAndStatusIn(
        @Param("bayId") UUID bayId, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate, 
        @Param("statuses") List<BaySchedule.ScheduleStatus> statuses);
    
    /**
     * Đếm số slot của bay trong ngày (chưa bị xóa)
     */
    @Query("SELECT COUNT(bs) FROM BaySchedule bs " +
           "WHERE bs.serviceBay.bayId = :bayId " +
           "AND bs.scheduleDate = :date " +
           "AND bs.isDeleted = false")
    long countByServiceBayBayIdAndScheduleDateAndIsDeletedFalse(
        @Param("bayId") UUID bayId, 
        @Param("date") LocalDate date);
}
