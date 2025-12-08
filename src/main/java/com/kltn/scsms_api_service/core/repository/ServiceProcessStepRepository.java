package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.ServiceProcessStep;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceProcessStepRepository extends JpaRepository<ServiceProcessStep, UUID> {

    /**
     * Tìm tất cả steps theo process
     */
    List<ServiceProcessStep> findByServiceProcess_IdOrderByStepOrderAsc(UUID processId);

    /**
     * Tìm step theo process và order
     */
    Optional<ServiceProcessStep> findByServiceProcess_IdAndStepOrder(UUID processId, Integer stepOrder);

    /**
     * Tìm step theo process và name
     */
    List<ServiceProcessStep> findByServiceProcess_IdAndNameContainingIgnoreCaseOrderByStepOrderAsc(
            UUID processId, String name);

    /**
     * Tìm steps bắt buộc theo process
     */
    List<ServiceProcessStep> findByServiceProcess_IdAndIsRequiredTrueOrderByStepOrderAsc(UUID processId);

    /**
     * Tìm steps có thể bỏ qua theo process
     */
    List<ServiceProcessStep> findByServiceProcess_IdAndIsRequiredFalseOrderByStepOrderAsc(UUID processId);

    /**
     * Tìm step đầu tiên của process
     */
    @Query("SELECT s FROM ServiceProcessStep s WHERE s.serviceProcess.id = :processId ORDER BY s.stepOrder ASC LIMIT 1")
    Optional<ServiceProcessStep> findFirstStepByProcessId(@Param("processId") UUID processId);

    /**
     * Tìm step cuối cùng của process
     */
    @Query("SELECT s FROM ServiceProcessStep s WHERE s.serviceProcess.id = :processId ORDER BY s.stepOrder DESC LIMIT 1")
    Optional<ServiceProcessStep> findLastStepByProcessId(@Param("processId") UUID processId);

    /**
     * Tìm step tiếp theo trong process
     */
    @Query("SELECT s FROM ServiceProcessStep s WHERE s.serviceProcess.id = :processId AND s.stepOrder > :currentOrder ORDER BY s.stepOrder ASC LIMIT 1")
    Optional<ServiceProcessStep> findNextStepByProcessIdAndOrder(@Param("processId") UUID processId,
            @Param("currentOrder") Integer currentOrder);

    /**
     * Tìm step trước đó trong process
     */
    @Query("SELECT s FROM ServiceProcessStep s WHERE s.serviceProcess.id = :processId AND s.stepOrder < :currentOrder ORDER BY s.stepOrder DESC LIMIT 1")
    Optional<ServiceProcessStep> findPreviousStepByProcessIdAndOrder(@Param("processId") UUID processId,
            @Param("currentOrder") Integer currentOrder);

    /**
     * Đếm số steps trong process
     */
    long countByServiceProcess_Id(UUID processId);

    /**
     * Tìm steps theo tên (search across all processes)
     */
    Page<ServiceProcessStep> findByNameContainingIgnoreCaseOrderByStepOrderAsc(String name, Pageable pageable);

    /**
     * Tìm steps theo process với pagination
     */
    Page<ServiceProcessStep> findByServiceProcess_IdOrderByStepOrderAsc(UUID processId, Pageable pageable);

    /**
     * Kiểm tra xem step order có tồn tại trong process không
     */
    boolean existsByServiceProcess_IdAndStepOrder(UUID processId, Integer stepOrder);

    /**
     * Tìm step theo process và step order (không bao gồm step hiện tại)
     */
    @Query("SELECT s FROM ServiceProcessStep s WHERE s.serviceProcess.id = :processId AND s.stepOrder = :stepOrder AND s.id != :excludeId")
    Optional<ServiceProcessStep> findByServiceProcess_IdAndStepOrderExcludingId(
            @Param("processId") UUID processId,
            @Param("stepOrder") Integer stepOrder,
            @Param("excludeId") UUID excludeId);
}