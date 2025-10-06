package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity quản lý phân công nhân viên và tài nguyên cho booking
 */
@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "booking_assignments", schema = GeneralConstant.DB_SCHEMA_DEV)
public class BookingAssignment extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "assignment_id", nullable = false)
    private UUID assignmentId;
    
    /**
     * Booking được phân công
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    
    /**
     * Nhân viên được phân công
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private User staff;
    
    /**
     * Vai trò của nhân viên trong booking này
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private StaffRole role;
    
    /**
     * Thời gian bắt đầu phân công
     */
    @Column(name = "assigned_from", nullable = false)
    private LocalDateTime assignedFrom;
    
    /**
     * Thời gian kết thúc phân công
     */
    @Column(name = "assigned_to")
    private LocalDateTime assignedTo;
    
    /**
     * Loại tài nguyên (BAY, LIFT, EQUIPMENT)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type")
    private ResourceType resourceType;
    
    /**
     * ID tài nguyên (bay/lift/equipment)
     */
    @Column(name = "resource_id")
    private UUID resourceId;
    
    /**
     * Tên tài nguyên (snapshot)
     */
    @Column(name = "resource_name", length = 255)
    private String resourceName;
    
    /**
     * Trạng thái phân công
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_status", nullable = false)
    @Builder.Default
    private AssignmentStatus assignmentStatus = AssignmentStatus.ASSIGNED;
    
    /**
     * Ghi chú phân công
     */
    @Column(name = "notes", length = 500)
    private String notes;
    
    /**
     * Thời gian bắt đầu thực tế
     */
    @Column(name = "actual_start_at")
    private LocalDateTime actualStartAt;
    
    /**
     * Thời gian kết thúc thực tế
     */
    @Column(name = "actual_end_at")
    private LocalDateTime actualEndAt;
    
    /**
     * Người phân công
     */
    @Column(name = "assigned_by", length = 255)
    private String assignedBy;
    
    /**
     * Thời gian phân công
     */
    @Column(name = "assigned_at", nullable = false)
    @Builder.Default
    private LocalDateTime assignedAt = LocalDateTime.now();
    
    // Business methods
    
    /**
     * Kiểm tra assignment có active không
     */
    public boolean isActive() {
        return assignmentStatus == AssignmentStatus.ASSIGNED || 
               assignmentStatus == AssignmentStatus.IN_PROGRESS;
    }
    
    /**
     * Kiểm tra assignment có hoàn thành không
     */
    public boolean isCompleted() {
        return assignmentStatus == AssignmentStatus.COMPLETED;
    }
    
    /**
     * Bắt đầu thực hiện assignment
     */
    public void startAssignment() {
        this.assignmentStatus = AssignmentStatus.IN_PROGRESS;
        this.actualStartAt = LocalDateTime.now();
    }
    
    /**
     * Hoàn thành assignment
     */
    public void completeAssignment() {
        this.assignmentStatus = AssignmentStatus.COMPLETED;
        this.actualEndAt = LocalDateTime.now();
    }
    
    /**
     * Hủy assignment
     */
    public void cancelAssignment(String reason) {
        this.assignmentStatus = AssignmentStatus.CANCELLED;
        this.notes = (this.notes != null ? this.notes + "\n" : "") + "Cancelled: " + reason;
    }
    
    /**
     * Tính thời gian thực tế (phút)
     */
    public Long getActualDurationMinutes() {
        if (actualStartAt != null && actualEndAt != null) {
            return java.time.Duration.between(actualStartAt, actualEndAt).toMinutes();
        }
        return null;
    }
    
    /**
     * Tính thời gian dự kiến (phút)
     */
    public Long getEstimatedDurationMinutes() {
        if (assignedFrom != null && assignedTo != null) {
            return java.time.Duration.between(assignedFrom, assignedTo).toMinutes();
        }
        return null;
    }
    
    /**
     * Enum cho vai trò nhân viên
     */
    public enum StaffRole {
        LEAD_TECH,      // Kỹ thuật viên chính
        ASSISTANT,      // Phụ tá
        CLEANER,        // Nhân viên vệ sinh
        SUPERVISOR,     // Giám sát
        MANAGER,        // Quản lý
        RECEPTIONIST    // Lễ tân
    }
    
    /**
     * Enum cho loại tài nguyên
     */
    public enum ResourceType {
        BAY,            // Bãi chăm sóc
        LIFT,           // Cầu nâng
        EQUIPMENT,      // Thiết bị
        TOOL,           // Dụng cụ
        ROOM            // Phòng
    }
    
    /**
     * Enum cho trạng thái phân công
     */
    public enum AssignmentStatus {
        ASSIGNED,       // Đã phân công
        IN_PROGRESS,    // Đang thực hiện
        COMPLETED,      // Hoàn thành
        CANCELLED,      // Đã hủy
        NO_SHOW         // Không thực hiện
    }
}
