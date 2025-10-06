package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.BookingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingAssignmentRepository extends JpaRepository<BookingAssignment, UUID> {
    
    /**
     * Tìm assignments theo booking
     */
    List<BookingAssignment> findByBooking_BookingIdOrderByAssignedAtAsc(UUID bookingId);
    
    /**
     * Tìm assignments theo nhân viên
     */
    List<BookingAssignment> findByStaff_UserIdOrderByAssignedFromAsc(UUID staffId);
    
    /**
     * Tìm assignments theo vai trò
     */
    List<BookingAssignment> findByRoleOrderByAssignedFromAsc(BookingAssignment.StaffRole role);
    
    /**
     * Tìm assignments theo trạng thái
     */
    List<BookingAssignment> findByAssignmentStatusOrderByAssignedFromAsc(BookingAssignment.AssignmentStatus status);
    
    /**
     * Tìm assignments theo nhân viên và trạng thái
     */
    List<BookingAssignment> findByStaff_UserIdAndAssignmentStatusOrderByAssignedFromAsc(
        UUID staffId, BookingAssignment.AssignmentStatus status);
    
    /**
     * Tìm assignments theo booking và vai trò
     */
    List<BookingAssignment> findByBooking_BookingIdAndRoleOrderByAssignedAtAsc(
        UUID bookingId, BookingAssignment.StaffRole role);
    
    /**
     * Tìm assignments trong khoảng thời gian
     */
    @Query("SELECT ba FROM BookingAssignment ba WHERE ba.staff.userId = :staffId " +
           "AND ((ba.assignedFrom <= :endDateTime AND (ba.assignedTo IS NULL OR ba.assignedTo >= :startDateTime))) " +
           "ORDER BY ba.assignedFrom ASC")
    List<BookingAssignment> findAssignmentsByStaffInTimeRange(
        @Param("staffId") UUID staffId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * Tìm assignments trùng thời gian
     */
    @Query("SELECT ba FROM BookingAssignment ba WHERE ba.staff.userId = :staffId " +
           "AND ba.assignmentId != :excludeAssignmentId " +
           "AND ((ba.assignedFrom < :endDateTime AND (ba.assignedTo IS NULL OR ba.assignedTo > :startDateTime))) " +
           "AND ba.assignmentStatus IN ('ASSIGNED', 'IN_PROGRESS')")
    List<BookingAssignment> findOverlappingAssignments(
        @Param("staffId") UUID staffId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime,
        @Param("excludeAssignmentId") UUID excludeAssignmentId);
    
    /**
     * Tìm assignments theo tài nguyên
     */
    List<BookingAssignment> findByResourceTypeAndResourceIdOrderByAssignedFromAsc(
        BookingAssignment.ResourceType resourceType, UUID resourceId);
    
    /**
     * Tìm assignments theo tài nguyên trong khoảng thời gian
     */
    @Query("SELECT ba FROM BookingAssignment ba WHERE ba.resourceType = :resourceType " +
           "AND ba.resourceId = :resourceId " +
           "AND ((ba.assignedFrom <= :endDateTime AND (ba.assignedTo IS NULL OR ba.assignedTo >= :startDateTime))) " +
           "ORDER BY ba.assignedFrom ASC")
    List<BookingAssignment> findAssignmentsByResourceInTimeRange(
        @Param("resourceType") BookingAssignment.ResourceType resourceType,
        @Param("resourceId") UUID resourceId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * Đếm assignments theo nhân viên
     */
    long countByStaff_UserId(UUID staffId);
    
    /**
     * Đếm assignments theo trạng thái
     */
    long countByAssignmentStatus(BookingAssignment.AssignmentStatus status);
    
    /**
     * Đếm assignments theo vai trò
     */
    long countByRole(BookingAssignment.StaffRole role);
    
    /**
     * Tìm nhân viên có ít assignment nhất trong khoảng thời gian
     */
    @Query("SELECT ba.staff.userId, COUNT(ba) as assignmentCount " +
           "FROM BookingAssignment ba " +
           "WHERE ba.role = :role " +
           "AND ((ba.assignedFrom <= :endDateTime AND (ba.assignedTo IS NULL OR ba.assignedTo >= :startDateTime))) " +
           "AND ba.assignmentStatus IN ('ASSIGNED', 'IN_PROGRESS') " +
           "GROUP BY ba.staff.userId " +
           "ORDER BY assignmentCount ASC")
    List<Object[]> findStaffWithLeastAssignments(
        @Param("role") BookingAssignment.StaffRole role,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * Tìm assignments hoàn thành trong khoảng thời gian
     */
    @Query("SELECT ba FROM BookingAssignment ba WHERE ba.staff.userId = :staffId " +
           "AND ba.actualEndAt BETWEEN :startDateTime AND :endDateTime " +
           "AND ba.assignmentStatus = 'COMPLETED' " +
           "ORDER BY ba.actualEndAt DESC")
    List<BookingAssignment> findCompletedAssignmentsByStaffInTimeRange(
        @Param("staffId") UUID staffId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * Tìm assignments theo booking và trạng thái
     */
    List<BookingAssignment> findByBooking_BookingIdAndAssignmentStatusOrderByAssignedAtAsc(
        UUID bookingId, BookingAssignment.AssignmentStatus status);
    
    /**
     * Tìm assignments active của nhân viên
     */
    @Query("SELECT ba FROM BookingAssignment ba WHERE ba.staff.userId = :staffId " +
           "AND ba.assignmentStatus IN ('ASSIGNED', 'IN_PROGRESS') " +
           "ORDER BY ba.assignedFrom ASC")
    List<BookingAssignment> findActiveAssignmentsByStaff(@Param("staffId") UUID staffId);
    
    /**
     * Tìm assignments theo người phân công
     */
    List<BookingAssignment> findByAssignedByOrderByAssignedAtDesc(String assignedBy);
    
    /**
     * Xóa assignments theo booking
     */
    void deleteByBooking_BookingId(UUID bookingId);
}
