package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.ServiceProcessTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceProcessTrackingRepository extends JpaRepository<ServiceProcessTracking, UUID> {

    /**
     * Tìm tracking theo booking
     */
    List<ServiceProcessTracking> findByBooking_BookingIdOrderByCreatedDateAsc(UUID bookingId);

    /**
     * Tìm tracking theo kỹ thuật viên
     */
    List<ServiceProcessTracking> findByTechnician_UserIdOrderByStartTimeDesc(UUID technicianId);

    /**
     * Tìm tracking theo slot
     */
    List<ServiceProcessTracking> findBySlot_SlotIdOrderByStartTimeDesc(UUID slotId);

    /**
     * Tìm tracking theo trạng thái
     */
    List<ServiceProcessTracking> findByStatusOrderByStartTimeDesc(ServiceProcessTracking.TrackingStatus status);

    /**
     * Tìm tracking theo booking và trạng thái
     */
    List<ServiceProcessTracking> findByBooking_BookingIdAndStatusOrderByCreatedDateAsc(
            UUID bookingId, ServiceProcessTracking.TrackingStatus status);

    /**
     * Tìm tracking theo kỹ thuật viên và trạng thái
     */
    List<ServiceProcessTracking> findByTechnician_UserIdAndStatusOrderByStartTimeDesc(
            UUID technicianId, ServiceProcessTracking.TrackingStatus status);

    /**
     * Tìm tracking theo slot và trạng thái
     */
    List<ServiceProcessTracking> findBySlot_SlotIdAndStatusOrderByStartTimeDesc(
            UUID slotId, ServiceProcessTracking.TrackingStatus status);

    /**
     * Tìm tracking theo service step
     */
    List<ServiceProcessTracking> findByServiceStep_IdOrderByStartTimeDesc(UUID stepId);

    /**
     * Tìm tracking theo khoảng thời gian
     */
    @Query("SELECT spt FROM ServiceProcessTracking spt WHERE " +
            "spt.startTime BETWEEN :startDate AND :endDate " +
            "ORDER BY spt.startTime DESC")
    List<ServiceProcessTracking> findByTimeRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Tìm tracking đang thực hiện
     */
    @Query("SELECT spt FROM ServiceProcessTracking spt WHERE " +
            "spt.status = 'IN_PROGRESS' " +
            "ORDER BY spt.startTime ASC")
    List<ServiceProcessTracking> findInProgressTrackings();

    /**
     * Tìm tracking theo kỹ thuật viên trong khoảng thời gian
     */
    @Query("SELECT spt FROM ServiceProcessTracking spt WHERE " +
            "spt.technician.userId = :technicianId AND " +
            "spt.startTime BETWEEN :startDate AND :endDate " +
            "ORDER BY spt.startTime DESC")
    List<ServiceProcessTracking> findByTechnicianAndTimeRange(
            @Param("technicianId") UUID technicianId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Tìm tracking theo slot trong khoảng thời gian
     */
    @Query("SELECT spt FROM ServiceProcessTracking spt WHERE " +
            "spt.slot.slotId = :slotId AND " +
            "spt.startTime BETWEEN :startDate AND :endDate " +
            "ORDER BY spt.startTime DESC")
    List<ServiceProcessTracking> findBySlotAndTimeRange(
            @Param("slotId") UUID slotId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Đếm tracking theo trạng thái
     */
    long countByStatus(ServiceProcessTracking.TrackingStatus status);

    /**
     * Đếm tracking theo kỹ thuật viên
     */
    long countByTechnician_UserId(UUID technicianId);

    /**
     * Đếm tracking theo slot
     */
    long countBySlot_SlotId(UUID slotId);

    /**
     * Đếm tracking theo booking
     */
    long countByBooking_BookingId(UUID bookingId);

    /**
     * Tính tổng thời gian thực tế theo kỹ thuật viên
     */
    @Query("SELECT COALESCE(SUM(spt.actualDuration), 0) FROM ServiceProcessTracking spt " +
            "WHERE spt.technician.userId = :technicianId AND " +
            "spt.status = 'COMPLETED' AND " +
            "spt.startTime BETWEEN :startDate AND :endDate")
    Long sumActualDurationByTechnician(
            @Param("technicianId") UUID technicianId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Tính tổng thời gian ước lượng theo kỹ thuật viên
     */
    @Query("SELECT COALESCE(SUM(spt.estimatedDuration), 0) FROM ServiceProcessTracking spt " +
            "WHERE spt.technician.userId = :technicianId AND " +
            "spt.startTime BETWEEN :startDate AND :endDate")
    Long sumEstimatedDurationByTechnician(
            @Param("technicianId") UUID technicianId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Tính hiệu suất trung bình theo kỹ thuật viên
     */
    @Query("SELECT AVG(CASE WHEN spt.actualDuration > 0 THEN " +
            "CAST(spt.estimatedDuration AS DOUBLE) / CAST(spt.actualDuration AS DOUBLE) " +
            "ELSE 0 END) FROM ServiceProcessTracking spt " +
            "WHERE spt.technician.userId = :technicianId AND " +
            "spt.status = 'COMPLETED' AND " +
            "spt.actualDuration > 0 AND " +
            "spt.startTime BETWEEN :startDate AND :endDate")
    Double getAverageEfficiencyByTechnician(
            @Param("technicianId") UUID technicianId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Tìm tracking có tiến độ thấp (cần theo dõi)
     */
    @Query("SELECT spt FROM ServiceProcessTracking spt WHERE " +
            "spt.status = 'IN_PROGRESS' AND " +
            "spt.progressPercent < :threshold AND " +
            "spt.startTime < :thresholdTime " +
            "ORDER BY spt.startTime ASC")
    List<ServiceProcessTracking> findLowProgressTrackings(
            @Param("threshold") java.math.BigDecimal threshold,
            @Param("thresholdTime") LocalDateTime thresholdTime);

    /**
     * Tìm tracking theo service step và trạng thái
     */
    List<ServiceProcessTracking> findByServiceStep_IdAndStatusOrderByStartTimeDesc(
            UUID stepId, ServiceProcessTracking.TrackingStatus status);

    /**
     * Tìm tracking hoàn thành trong khoảng thời gian
     */
    @Query("SELECT spt FROM ServiceProcessTracking spt WHERE " +
            "spt.status = 'COMPLETED' AND " +
            "spt.endTime BETWEEN :startDate AND :endDate " +
            "ORDER BY spt.endTime DESC")
    List<ServiceProcessTracking> findCompletedTrackingsInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Tìm tracking theo booking và service step
     */
    List<ServiceProcessTracking> findByBooking_BookingIdAndServiceStep_IdOrderByCreatedDateAsc(
            UUID bookingId, UUID stepId);

    /**
     * Kiểm tra xem có tracking nào đang thực hiện cho booking không
     */
    @Query("SELECT COUNT(spt) > 0 FROM ServiceProcessTracking spt WHERE " +
            "spt.booking.bookingId = :bookingId AND " +
            "spt.status = 'IN_PROGRESS'")
    boolean existsInProgressTrackingForBooking(@Param("bookingId") UUID bookingId);

    /**
     * Tìm tracking cuối cùng của booking
     */
    @Query("SELECT spt FROM ServiceProcessTracking spt WHERE " +
            "spt.booking.bookingId = :bookingId " +
            "ORDER BY spt.createdDate DESC LIMIT 1")
    ServiceProcessTracking findLatestTrackingForBooking(@Param("bookingId") UUID bookingId);

    /**
     * Tìm tracking theo kỹ thuật viên và booking
     */
    List<ServiceProcessTracking> findByTechnician_UserIdAndBooking_BookingIdOrderByStartTimeDesc(
            UUID technicianId, UUID bookingId);
}
