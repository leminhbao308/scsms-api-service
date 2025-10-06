package com.kltn.scsms_api_service.core.dto.bookingManagement.request;

import com.kltn.scsms_api_service.core.entity.BookingAssignment;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request DTO để tạo booking assignment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingAssignmentRequest {
    
    @NotNull(message = "Staff ID is required")
    private UUID staffId;
    
    @NotNull(message = "Role is required")
    private BookingAssignment.StaffRole role;
    
    @NotNull(message = "Assigned from time is required")
    private LocalDateTime assignedFrom;
    
    private LocalDateTime assignedTo;
    
    private BookingAssignment.ResourceType resourceType;
    private UUID resourceId;
    private String resourceName;
    
    private String notes;
    private String assignedBy;
}
