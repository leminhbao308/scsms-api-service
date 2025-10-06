package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.ServiceSlot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public interface ServiceSlotRepository extends JpaRepository<ServiceSlot, UUID> {
    
    /**
     * Tìm tất cả slot của một chi nhánh
     */
    List<ServiceSlot> findByBranch_BranchIdOrderBySlotDateAscStartTimeAsc(UUID branchId);
    
    /**
     * Tìm slot theo chi nhánh và ngày
     */
    List<ServiceSlot> findByBranch_BranchIdAndSlotDateOrderByStartTimeAsc(UUID branchId, LocalDate slotDate);
    
    /**
     * Tìm slot khả dụng theo chi nhánh và ngày
     */
    List<ServiceSlot> findByBranch_BranchIdAndSlotDateAndStatusOrderByStartTimeAsc(
        UUID branchId, LocalDate slotDate, ServiceSlot.SlotStatus status);
    
    /**
     * Tìm slot theo chi nhánh, ngày và loại slot
     */
    List<ServiceSlot> findByBranch_BranchIdAndSlotDateAndSlotCategoryOrderByStartTimeAsc(
        UUID branchId, LocalDate slotDate, ServiceSlot.SlotCategory slotCategory);
    
    /**
     * Tìm slot khả dụng theo chi nhánh, ngày và loại slot
     */
    List<ServiceSlot> findByBranch_BranchIdAndSlotDateAndSlotCategoryAndStatusOrderByStartTimeAsc(
        UUID branchId, LocalDate slotDate, ServiceSlot.SlotCategory slotCategory, ServiceSlot.SlotStatus status);
    
    /**
     * Tìm slot theo khoảng thời gian
     */
    @Query("SELECT s FROM ServiceSlot s WHERE s.branch.branchId = :branchId " +
           "AND s.slotDate = :slotDate " +
           "AND s.status = :status " +
           "AND ((s.startTime < :endTime AND s.endTime > :startTime)) " +
           "ORDER BY s.startTime ASC")
    List<ServiceSlot> findAvailableSlotsInTimeRange(
        @Param("branchId") UUID branchId,
        @Param("slotDate") LocalDate slotDate,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("status") ServiceSlot.SlotStatus status);
    
    /**
     * Tìm slot trùng thời gian
     */
    @Query("SELECT s FROM ServiceSlot s WHERE s.branch.branchId = :branchId " +
           "AND s.slotDate = :slotDate " +
           "AND s.slotId != :excludeSlotId " +
           "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    List<ServiceSlot> findOverlappingSlots(
        @Param("branchId") UUID branchId,
        @Param("slotDate") LocalDate slotDate,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("excludeSlotId") UUID excludeSlotId);
    
    /**
     * Đếm số slot theo chi nhánh và ngày
     */
    long countByBranch_BranchIdAndSlotDate(UUID branchId, LocalDate slotDate);
    
    /**
     * Đếm số slot khả dụng theo chi nhánh và ngày
     */
    long countByBranch_BranchIdAndSlotDateAndStatus(UUID branchId, LocalDate slotDate, ServiceSlot.SlotStatus status);
    
    /**
     * Tìm slot theo khoảng ngày
     */
    List<ServiceSlot> findByBranch_BranchIdAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(
        UUID branchId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Tìm slot VIP khả dụng
     */
    List<ServiceSlot> findByBranch_BranchIdAndSlotDateAndSlotCategoryAndStatusOrderByPriorityOrderAscStartTimeAsc(
        UUID branchId, LocalDate slotDate, ServiceSlot.SlotCategory slotCategory, ServiceSlot.SlotStatus status);
    
    /**
     * Tìm slot theo priority order
     */
    List<ServiceSlot> findByBranch_BranchIdAndSlotDateOrderByPriorityOrderAscStartTimeAsc(UUID branchId, LocalDate slotDate);
    
    /**
     * Kiểm tra slot có tồn tại không
     */
    boolean existsByBranch_BranchIdAndSlotDateAndStartTimeAndEndTime(
        UUID branchId, LocalDate slotDate, LocalTime startTime, LocalTime endTime);
    
    /**
     * Tìm slot theo ID và chi nhánh
     */
    Optional<ServiceSlot> findBySlotIdAndBranch_BranchId(UUID slotId, UUID branchId);
    
    /**
     * Phân trang slot theo chi nhánh
     */
    Page<ServiceSlot> findByBranch_BranchIdOrderBySlotDateDescStartTimeAsc(UUID branchId, Pageable pageable);
    
    /**
     * Tìm slot trong tương lai
     */
    @Query("SELECT s FROM ServiceSlot s WHERE s.branch.branchId = :branchId " +
           "AND s.slotDate >= :fromDate " +
           "AND s.status = :status " +
           "ORDER BY s.slotDate ASC, s.startTime ASC")
    List<ServiceSlot> findFutureAvailableSlots(
        @Param("branchId") UUID branchId,
        @Param("fromDate") LocalDate fromDate,
        @Param("status") ServiceSlot.SlotStatus status);
}
