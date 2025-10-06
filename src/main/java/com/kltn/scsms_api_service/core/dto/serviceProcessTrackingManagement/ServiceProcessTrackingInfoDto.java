package com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement;

import com.kltn.scsms_api_service.core.entity.ServiceProcessTracking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO thông tin chi tiết về theo dõi quá trình thực hiện dịch vụ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProcessTrackingInfoDto {
    
    private UUID trackingId;
    
    // Booking information
    private UUID bookingId;
    private String bookingCode;
    private String customerName;
    private String customerPhone;
    private String vehicleLicensePlate;
    
    // Service step information
    private UUID serviceStepId;
    private String serviceStepName;
    private String serviceStepDescription;
    private Integer serviceStepOrder;
    private Integer estimatedTime;
    private Boolean isRequired;
    
    // Technician information
    private UUID technicianId;
    private String technicianName;
    private String technicianCode;
    
    // Slot information
    private UUID slotId;
    private String slotName;
    private String slotCode;
    
    // Timing information
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer estimatedDuration;
    private Integer actualDuration;
    
    // Status and progress
    private ServiceProcessTracking.TrackingStatus status;
    private BigDecimal progressPercent;
    
    // Additional information
    private String notes;
    private String evidenceMediaUrls;
    
    // Last update information
    private UUID lastUpdatedBy;
    private String lastUpdatedByName;
    private LocalDateTime lastUpdatedAt;
    
    // Audit information
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private String createdBy;
    private String modifiedBy;
    
    // Calculated fields
    private BigDecimal efficiency;
    private String statusDisplay;
    private String durationDisplay;
    private String progressDisplay;
    
    // Helper methods for display
    public String getStatusDisplay() {
        if (status == null) return "Unknown";
        return switch (status) {
            case PENDING -> "Chờ thực hiện";
            case IN_PROGRESS -> "Đang thực hiện";
            case COMPLETED -> "Hoàn thành";
            case CANCELLED -> "Đã hủy";
        };
    }
    
    public String getDurationDisplay() {
        if (actualDuration == null) {
            return estimatedDuration != null ? estimatedDuration + " phút (ước tính)" : "Chưa xác định";
        }
        return actualDuration + " phút";
    }
    
    public String getProgressDisplay() {
        if (progressPercent == null) return "0%";
        return progressPercent + "%";
    }
    
    public boolean isInProgress() {
        return status == ServiceProcessTracking.TrackingStatus.IN_PROGRESS;
    }
    
    public boolean isCompleted() {
        return status == ServiceProcessTracking.TrackingStatus.COMPLETED;
    }
    
    public boolean isCancelled() {
        return status == ServiceProcessTracking.TrackingStatus.CANCELLED;
    }
    
    public boolean isPending() {
        return status == ServiceProcessTracking.TrackingStatus.PENDING;
    }
}
