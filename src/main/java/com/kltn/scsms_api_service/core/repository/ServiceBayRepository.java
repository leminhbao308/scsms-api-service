package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.ServiceBay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceBayRepository extends JpaRepository<ServiceBay, UUID> {
    
    /**
     * Tìm tất cả bay của một chi nhánh
     */
    List<ServiceBay> findByBranch_BranchIdOrderByDisplayOrderAscBayNameAsc(UUID branchId);
    
    /**
     * Tìm bay theo chi nhánh và trạng thái
     */
    List<ServiceBay> findByBranch_BranchIdAndStatusOrderByDisplayOrderAscBayNameAsc(
        UUID branchId, ServiceBay.BayStatus status);
    
    /**
     * Tìm bay theo loại bay
     */
    List<ServiceBay> findByBayTypeOrderByDisplayOrderAscBayNameAsc(ServiceBay.BayType bayType);
    
    /**
     * Tìm bay theo chi nhánh và loại bay
     */
    List<ServiceBay> findByBranch_BranchIdAndBayTypeOrderByDisplayOrderAscBayNameAsc(
        UUID branchId, ServiceBay.BayType bayType);
    
    
    /**
     * Tìm bay theo tên
     */
    Optional<ServiceBay> findByBayName(String bayName);
    
    /**
     * Tìm bay theo mã bay
     */
    Optional<ServiceBay> findByBayCode(String bayCode);
    
    /**
     * Kiểm tra tên bay đã tồn tại trong chi nhánh chưa
     */
    boolean existsByBranch_BranchIdAndBayNameAndBayIdNot(UUID branchId, String bayName, UUID bayId);
    
    /**
     * Kiểm tra tên bay đã tồn tại trong chi nhánh chưa (không loại trừ bay nào)
     */
    boolean existsByBranch_BranchIdAndBayName(UUID branchId, String bayName);
    
    /**
     * Kiểm tra mã bay đã tồn tại chưa
     */
    boolean existsByBayCodeAndBayIdNot(String bayCode, UUID bayId);
    
    /**
     * Kiểm tra mã bay đã tồn tại chưa (không loại trừ bay nào)
     */
    boolean existsByBayCode(String bayCode);
    
    /**
     * Tìm bay có thể sử dụng trong khoảng thời gian
     */
    @Query("SELECT DISTINCT b FROM ServiceBay b " +
           "WHERE b.branch.branchId = :branchId " +
           "AND b.status = 'ACTIVE' " +
           "AND b.bayId NOT IN (" +
           "    SELECT bk.serviceBay.bayId FROM Booking bk " +
           "    WHERE bk.status IN ('CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS', 'PAUSED') " +
           "    AND ((bk.scheduledStartAt < :endTime AND bk.scheduledEndAt > :startTime))" +
           ") " +
           "ORDER BY b.displayOrder ASC, b.bayName ASC")
    List<ServiceBay> findAvailableBaysInTimeRange(
        @Param("branchId") UUID branchId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);
    
    /**
     * Tìm bay có thể sử dụng theo loại bay trong khoảng thời gian
     */
    @Query("SELECT DISTINCT b FROM ServiceBay b " +
           "WHERE b.branch.branchId = :branchId " +
           "AND b.bayType = :bayType " +
           "AND b.status = 'ACTIVE' " +
           "AND b.bayId NOT IN (" +
           "    SELECT bk.serviceBay.bayId FROM Booking bk " +
           "    WHERE bk.status IN ('CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS', 'PAUSED') " +
           "    AND ((bk.scheduledStartAt < :endTime AND bk.scheduledEndAt > :startTime))" +
           ") " +
           "ORDER BY b.displayOrder ASC, b.bayName ASC")
    List<ServiceBay> findAvailableBaysByTypeInTimeRange(
        @Param("branchId") UUID branchId,
        @Param("bayType") ServiceBay.BayType bayType,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);
    
    /**
     * Kiểm tra bay có khả dụng trong khoảng thời gian không
     */
    @Query("SELECT COUNT(bk) = 0 FROM Booking bk " +
           "WHERE bk.serviceBay.bayId = :bayId " +
           "AND bk.status IN ('CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS', 'PAUSED') " +
           "AND ((bk.scheduledStartAt < :endTime AND bk.scheduledEndAt > :startTime))")
    boolean isBayAvailableInTimeRange(
        @Param("bayId") UUID bayId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);
    
    /**
     * Đếm số bay theo chi nhánh
     */
    long countByBranch_BranchId(UUID branchId);
    
    /**
     * Đếm số bay theo chi nhánh và trạng thái
     */
    long countByBranch_BranchIdAndStatus(UUID branchId, ServiceBay.BayStatus status);
    
    /**
     * Đếm số bay theo chi nhánh và loại bay
     */
    long countByBranch_BranchIdAndBayType(UUID branchId, ServiceBay.BayType bayType);
    
    /**
     * Tìm bay theo từ khóa
     */
    @Query("SELECT b FROM ServiceBay b " +
           "WHERE (LOWER(b.bayName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.bayCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY b.displayOrder ASC, b.bayName ASC")
    List<ServiceBay> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * Tìm bay theo từ khóa trong chi nhánh
     */
    @Query("SELECT b FROM ServiceBay b " +
           "WHERE b.branch.branchId = :branchId " +
           "AND (LOWER(b.bayName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.bayCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY b.displayOrder ASC, b.bayName ASC")
    List<ServiceBay> searchByKeywordInBranch(@Param("branchId") UUID branchId, @Param("keyword") String keyword);
    
    /**
     * Lấy thống kê bay theo chi nhánh
     */
    @Query("SELECT b.bayType, COUNT(b) FROM ServiceBay b " +
           "WHERE b.branch.branchId = :branchId " +
           "GROUP BY b.bayType")
    List<Object[]> getBayStatisticsByBranch(@Param("branchId") UUID branchId);
    
    /**
     * Lấy thống kê bay theo trạng thái
     */
    @Query("SELECT b.status, COUNT(b) FROM ServiceBay b " +
           "WHERE b.branch.branchId = :branchId " +
           "GROUP BY b.status")
    List<Object[]> getBayStatusStatisticsByBranch(@Param("branchId") UUID branchId);
}
