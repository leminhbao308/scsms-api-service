package com.kltn.scsms_api_service.core.dto.bookingManagement;

import com.kltn.scsms_api_service.core.entity.BookingAssignment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO chứa thông tin chi tiết của booking assignment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingAssignmentInfoDto {
    
    private UUID assignmentId;
    private UUID bookingId;
    
    // Staff information
    private UUID staffId;
    private String staffName;
    private String staffCode;
    private String staffPhone;
    private String staffEmail;
    
    // Assignment details
    private BookingAssignment.StaffRole role;
    private LocalDateTime assignedFrom;
    private LocalDateTime assignedTo;
    private LocalDateTime actualStartAt;
    private LocalDateTime actualEndAt;
    
    // Resource information
    private BookingAssignment.ResourceType resourceType;
    private UUID resourceId;
    private String resourceName;
    
    // Status information
    private BookingAssignment.AssignmentStatus assignmentStatus;
    private String notes;
    
    // Assignment tracking
    private String assignedBy;
    private LocalDateTime assignedAt;
    
    // Audit information
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String modifiedBy;
    
    // Computed fields
    private Boolean isActive;
    private Boolean isCompleted;
    private Long actualDurationMinutes;
    private Long estimatedDurationMinutes;
}
