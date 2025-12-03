package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.BookingDraft;
import com.kltn.scsms_api_service.core.entity.enumAttribute.DraftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingDraftRepository extends JpaRepository<BookingDraft, UUID> {
    
    /**
     * Tìm draft theo session ID và status
     * Dùng để tìm draft đang active của một session
     */
    Optional<BookingDraft> findBySessionIdAndStatus(String sessionId, DraftStatus status);
    
    /**
     * Tìm draft của customer theo status, sắp xếp theo updated_at DESC
     * Dùng để tìm draft mới nhất của customer (recovery)
     */
    List<BookingDraft> findByCustomerIdAndStatusOrderByModifiedDateDesc(
        UUID customerId, 
        DraftStatus status
    );
    
    /**
     * Tìm các draft bị abandoned (cho cleanup job)
     * Điều kiện:
     * - status = IN_PROGRESS
     * - expires_at < now HOẶC last_activity_at < expiredThreshold
     */
    @Query("SELECT d FROM BookingDraft d WHERE " +
           "d.status = 'IN_PROGRESS' AND " +
           "(d.expiresAt < :now OR d.lastActivityAt < :expiredThreshold)")
    List<BookingDraft> findAbandonedDrafts(
        @Param("now") LocalDateTime now,
        @Param("expiredThreshold") LocalDateTime expiredThreshold
    );
    
    /**
     * Tìm các draft cũ theo status và updated_at
     * Dùng để xóa các draft ABANDONED cũ (> 30 ngày)
     */
    List<BookingDraft> findByStatusAndModifiedDateBefore(
        DraftStatus status,
        LocalDateTime threshold
    );
    
    /**
     * Tìm các draft đã hết hạn
     */
    List<BookingDraft> findByExpiresAtBeforeAndStatus(
        LocalDateTime now,
        DraftStatus status
    );
    
    /**
     * Tìm tất cả draft IN_PROGRESS của customer
     * Dùng để mark as ABANDONED khi tạo draft mới
     */
    List<BookingDraft> findByCustomerIdAndStatus(UUID customerId, DraftStatus status);
}

