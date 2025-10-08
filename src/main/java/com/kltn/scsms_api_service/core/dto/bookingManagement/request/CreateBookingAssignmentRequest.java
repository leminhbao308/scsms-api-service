package com.kltn.scsms_api_service.core.dto.bookingManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("staff_id")
    private UUID staffId;
    
    @NotNull(message = "Role is required")
    @JsonProperty("role")
    private BookingAssignment.StaffRole role;
    
    @NotNull(message = "Assigned from time is required")
    @JsonProperty("assigned_from")
    private LocalDateTime assignedFrom;
    
    @JsonProperty("assigned_to")
    private LocalDateTime assignedTo;
    
    @JsonProperty("resource_type")
    private BookingAssignment.ResourceType resourceType;
    @JsonProperty("resource_id")
    private UUID resourceId;
    @JsonProperty("resource_name")
    private String resourceName;
    
    @JsonProperty("notes")
    private String notes;
    @JsonProperty("assigned_by")
    private String assignedBy;
}
