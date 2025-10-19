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
     * Tìm tracking theo bay
     */
    List<ServiceProcessTracking> findByBay_BayIdOrderByStartTimeDesc(UUID bayId);

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
     * Tìm tracking theo bay và trạng thái
     */
    List<ServiceProcessTracking> findByBay_BayIdAndStatusOrderByStartTimeDesc(
            UUID bayId, ServiceProcessTracking.TrackingStatus status);

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
     * Tìm tracking theo bay trong khoảng thời gian
     */
    @Query("SELECT spt FROM ServiceProcessTracking spt WHERE " +
            "spt.bay.bayId = :bayId AND " +
            "spt.startTime BETWEEN :startDate AND :endDate " +
            "ORDER BY spt.startTime DESC")
    List<ServiceProcessTracking> findByBayAndTimeRange(
            @Param("bayId") UUID bayId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Đếm tracking theo trạng thái
     */
    long countByStatus(ServiceProcessTracking.TrackingStatus status);

    /**
     * Đếm tracking theo bay
     */
    long countByBay_BayId(UUID bayId);

    /**
     * Đếm tracking theo booking
     */
    long countByBooking_BookingId(UUID bookingId);


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

}
