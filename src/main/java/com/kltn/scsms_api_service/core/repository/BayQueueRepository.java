package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.BayQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository cho BayQueue entity
 * Cung cấp các method để thao tác với hàng chờ của bay
 */
@Repository
public interface BayQueueRepository extends JpaRepository<BayQueue, UUID> {
    
    /**
     * Tìm tất cả queue entries của một bay (active only)
     */
    @Query("SELECT bq FROM BayQueue bq WHERE bq.bayId = :bayId AND bq.isActive = true ORDER BY bq.queuePosition ASC")
    List<BayQueue> findActiveByBayId(@Param("bayId") UUID bayId);
    
    /**
     * Tìm tất cả queue entries của một bay (bao gồm cả inactive)
     */
    @Query("SELECT bq FROM BayQueue bq WHERE bq.bayId = :bayId ORDER BY bq.queuePosition ASC")
    List<BayQueue> findByBayId(@Param("bayId") UUID bayId);
    
    /**
     * Tìm queue entry của một booking
     */
    @Query("SELECT bq FROM BayQueue bq WHERE bq.bookingId = :bookingId AND bq.isActive = true")
    Optional<BayQueue> findActiveByBookingId(@Param("bookingId") UUID bookingId);
    
    /**
     * Tìm vị trí cuối cùng trong hàng chờ của một bay
     */
    @Query("SELECT COALESCE(MAX(bq.queuePosition), 0) FROM BayQueue bq WHERE bq.bayId = :bayId AND bq.isActive = true")
    Integer findLastQueuePosition(@Param("bayId") UUID bayId);
    
    /**
     * Đếm số lượng booking đang chờ trong một bay
     */
    @Query("SELECT COUNT(bq) FROM BayQueue bq WHERE bq.bayId = :bayId AND bq.isActive = true")
    Long countActiveByBayId(@Param("bayId") UUID bayId);
    
    /**
     * Tìm queue entries có vị trí lớn hơn hoặc bằng một vị trí cụ thể
     */
    @Query("SELECT bq FROM BayQueue bq WHERE bq.bayId = :bayId AND bq.queuePosition >= :position AND bq.isActive = true ORDER BY bq.queuePosition ASC")
    List<BayQueue> findByBayIdAndPositionGreaterThanEqual(@Param("bayId") UUID bayId, @Param("position") Integer position);
    
    /**
     * Tìm queue entries có thời gian bắt đầu dự kiến trong khoảng thời gian
     */
    @Query("SELECT bq FROM BayQueue bq WHERE bq.bayId = :bayId AND bq.estimatedStartTime BETWEEN :startTime AND :endTime AND bq.isActive = true ORDER BY bq.queuePosition ASC")
    List<BayQueue> findByBayIdAndEstimatedStartTimeBetween(@Param("bayId") UUID bayId, 
                                                          @Param("startTime") LocalDateTime startTime, 
                                                          @Param("endTime") LocalDateTime endTime);
    
    /**
     * Tìm queue entries sắp được phục vụ (vị trí 1-3)
     */
    @Query("SELECT bq FROM BayQueue bq WHERE bq.bayId = :bayId AND bq.queuePosition <= 3 AND bq.isActive = true ORDER BY bq.queuePosition ASC")
    List<BayQueue> findUpcomingByBayId(@Param("bayId") UUID bayId);
    
    /**
     * Tìm queue entry theo bay và booking
     */
    @Query("SELECT bq FROM BayQueue bq WHERE bq.bayId = :bayId AND bq.bookingId = :bookingId AND bq.isActive = true")
    Optional<BayQueue> findByBayIdAndBookingId(@Param("bayId") UUID bayId, @Param("bookingId") UUID bookingId);
    
    /**
     * Tìm tất cả queue entries của một booking (có thể có nhiều nếu chuyển bay)
     */
    @Query("SELECT bq FROM BayQueue bq WHERE bq.bookingId = :bookingId ORDER BY bq.createdDate DESC")
    List<BayQueue> findByBookingId(@Param("bookingId") UUID bookingId);
    
    /**
     * Tìm queue entries có thời gian hoàn thành dự kiến trước một thời điểm
     */
    @Query("SELECT bq FROM BayQueue bq WHERE bq.bayId = :bayId AND bq.estimatedCompletionTime <= :completionTime AND bq.isActive = true ORDER BY bq.queuePosition ASC")
    List<BayQueue> findByBayIdAndEstimatedCompletionTimeBefore(@Param("bayId") UUID bayId, @Param("completionTime") LocalDateTime completionTime);
    
    /**
     * Tìm queue entries có thời gian bắt đầu dự kiến sau một thời điểm
     */
    @Query("SELECT bq FROM BayQueue bq WHERE bq.bayId = :bayId AND bq.estimatedStartTime >= :startTime AND bq.isActive = true ORDER BY bq.queuePosition ASC")
    List<BayQueue> findByBayIdAndEstimatedStartTimeAfter(@Param("bayId") UUID bayId, @Param("startTime") LocalDateTime startTime);
}
