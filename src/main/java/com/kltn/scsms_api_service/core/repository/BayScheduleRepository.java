package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.BaySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
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
     * Tìm tất cả schedule của bay trong ngày
     */
    List<BaySchedule> findByServiceBayBayIdAndScheduleDateOrderByStartTime(UUID bayId, LocalDate date);
    
    /**
     * Tìm schedule theo bay, ngày và giờ bắt đầu
     */
    Optional<BaySchedule> findByServiceBayBayIdAndScheduleDateAndStartTime(UUID bayId, LocalDate date, LocalTime startTime);
    
    /**
     * Tìm các slot available của bay trong ngày
     */
    List<BaySchedule> findByServiceBayBayIdAndScheduleDateAndStatusOrderByStartTime(
        UUID bayId, LocalDate date, BaySchedule.ScheduleStatus status);
    
    /**
     * Tìm các slot available của tất cả bay trong chi nhánh trong ngày
     */
    @Query("SELECT bs FROM BaySchedule bs " +
           "JOIN bs.serviceBay sb " +
           "WHERE sb.branch.branchId = :branchId " +
           "AND bs.scheduleDate = :date " +
           "AND bs.status = :status " +
           "ORDER BY sb.displayOrder, bs.startTime")
    List<BaySchedule> findByBranchIdAndDateAndStatus(
        @Param("branchId") UUID branchId, 
        @Param("date") LocalDate date, 
        @Param("status") BaySchedule.ScheduleStatus status);
    
    /**
     * Kiểm tra slot có available không
     */
    boolean existsByServiceBayBayIdAndScheduleDateAndStartTimeAndStatus(
        UUID bayId, LocalDate date, LocalTime startTime, BaySchedule.ScheduleStatus status);
    
    /**
     * Tìm các slot bị conflict trong khoảng thời gian
     */
    @Query("SELECT bs FROM BaySchedule bs " +
           "WHERE bs.serviceBay.bayId = :bayId " +
           "AND bs.scheduleDate = :date " +
           "AND bs.status IN ('BOOKED', 'IN_PROGRESS') " +
           "AND ((bs.startTime < :endTime AND bs.endTime > :startTime))")
    List<BaySchedule> findConflictingSlots(
        @Param("bayId") UUID bayId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime);
    
    /**
     * Tìm các slot trong khoảng thời gian
     */
    @Query("SELECT bs FROM BaySchedule bs " +
           "WHERE bs.serviceBay.bayId = :bayId " +
           "AND bs.scheduleDate = :date " +
           "AND bs.startTime >= :startTime " +
           "AND bs.endTime <= :endTime " +
           "ORDER BY bs.startTime")
    List<BaySchedule> findSlotsInTimeRange(
        @Param("bayId") UUID bayId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime);
    
    /**
     * Tìm schedule theo booking
     */
    List<BaySchedule> findByBookingBookingId(UUID bookingId);
    
    /**
     * Đếm số slot available của bay trong ngày
     */
    long countByServiceBayBayIdAndScheduleDateAndStatus(
        UUID bayId, LocalDate date, BaySchedule.ScheduleStatus status);
    
    /**
     * Tìm các slot hoàn thành sớm trong ngày
     */
    @Query("SELECT bs FROM BaySchedule bs " +
           "WHERE bs.serviceBay.bayId = :bayId " +
           "AND bs.scheduleDate = :date " +
           "AND bs.status = 'COMPLETED' " +
           "AND bs.actualEndTime < bs.endTime " +
           "ORDER BY bs.actualEndTime")
    List<BaySchedule> findEarlyCompletedSlots(
        @Param("bayId") UUID bayId,
        @Param("date") LocalDate date);
    
    /**
     * Xóa tất cả schedule của bay trong ngày (dùng khi tạo lại lịch)
     */
    void deleteByServiceBayBayIdAndScheduleDate(UUID bayId, LocalDate date);
    
    /**
     * Tìm các slot có thể mở rộng (hoàn thành sớm)
     */
    @Query("SELECT bs FROM BaySchedule bs " +
           "WHERE bs.serviceBay.branch.branchId = :branchId " +
           "AND bs.scheduleDate = :date " +
           "AND bs.status = 'COMPLETED' " +
           "AND bs.actualEndTime < bs.endTime " +
           "AND bs.actualEndTime >= :fromTime " +
           "ORDER BY bs.actualEndTime")
    List<BaySchedule> findExpandableSlots(
        @Param("branchId") UUID branchId,
        @Param("date") LocalDate date,
        @Param("fromTime") LocalTime fromTime);
}
