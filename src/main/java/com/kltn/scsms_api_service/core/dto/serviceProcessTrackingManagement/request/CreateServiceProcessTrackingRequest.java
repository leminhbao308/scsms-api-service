package com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.ServiceProcessTracking;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO để tạo mới tracking quá trình thực hiện dịch vụ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceProcessTrackingRequest {
    
    @NotNull(message = "Booking ID không được để trống")
    @JsonProperty("booking_id")
    private UUID bookingId;
    
    @NotNull(message = "Service Step ID không được để trống")
    @JsonProperty("service_step_id")
    private UUID serviceStepId;
    
    @JsonProperty("bay_id")
    private UUID bayId;
    
    @JsonProperty("status")
    private ServiceProcessTracking.TrackingStatus status;
    
    // Removed: technician_id - technicians are assigned to bays
    // Removed: estimated_duration - simplified tracking
    // Removed: progress_percent - simplified tracking
    
    @JsonProperty("notes")
    private String notes;
    
    @JsonProperty("evidence_media_urls")
    private String evidenceMediaUrls;
}
