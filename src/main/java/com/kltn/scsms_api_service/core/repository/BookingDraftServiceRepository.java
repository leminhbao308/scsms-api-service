package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.DraftService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository cho DraftService (quan hệ nhiều-nhiều giữa BookingDraft và Service)
 */
@Repository
public interface BookingDraftServiceRepository extends JpaRepository<DraftService, UUID> {
    
    /**
     * Tìm tất cả dịch vụ của một draft (fetch join service để có thể lấy serviceId)
     */
    @Query("SELECT d FROM DraftService d LEFT JOIN FETCH d.service WHERE d.draftId = :draftId ORDER BY d.createdDate ASC")
    List<DraftService> findByDraftId(@Param("draftId") UUID draftId);
    
    /**
     * Xóa tất cả dịch vụ của một draft
     */
    @Modifying
    @Query("DELETE FROM DraftService d WHERE d.draftId = :draftId")
    void deleteByDraftId(@Param("draftId") UUID draftId);
    
    /**
     * Kiểm tra draft đã có service này chưa
     */
    @Query("SELECT COUNT(d) > 0 FROM DraftService d WHERE d.draftId = :draftId AND d.serviceId = :serviceId")
    boolean existsByDraftIdAndServiceId(@Param("draftId") UUID draftId, @Param("serviceId") UUID serviceId);
    
    /**
     * Xóa một service cụ thể khỏi draft
     */
    @Modifying
    @Query("DELETE FROM DraftService d WHERE d.draftId = :draftId AND d.serviceId = :serviceId")
    void deleteByDraftIdAndServiceId(@Param("draftId") UUID draftId, @Param("serviceId") UUID serviceId);
}

