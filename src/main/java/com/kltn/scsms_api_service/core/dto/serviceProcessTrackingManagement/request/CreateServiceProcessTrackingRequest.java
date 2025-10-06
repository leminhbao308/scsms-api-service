package com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.request;

import com.kltn.scsms_api_service.core.entity.ServiceProcessTracking;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private UUID bookingId;
    
    @NotNull(message = "Service Step ID không được để trống")
    private UUID serviceStepId;
    
    @NotNull(message = "Technician ID không được để trống")
    private UUID technicianId;
    
    @NotNull(message = "Slot ID không được để trống")
    private UUID slotId;
    
    @Positive(message = "Thời gian ước lượng phải lớn hơn 0")
    private Integer estimatedDuration;
    
    private ServiceProcessTracking.TrackingStatus status;
    
    private BigDecimal progressPercent;
    
    private String notes;
    
    private String evidenceMediaUrls;
}
