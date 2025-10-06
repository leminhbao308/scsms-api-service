package com.kltn.scsms_api_service.core.dto.serviceSlotManagement;

import com.kltn.scsms_api_service.core.entity.ServiceSlot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceSlotInfoDto {
    
    private UUID slotId;
    private UUID branchId;
    private String branchName;
    private String branchCode;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private ServiceSlot.SlotCategory slotCategory;
    private ServiceSlot.SlotStatus status;
    private Integer priorityOrder;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
    private Boolean isDeleted;
    
    // Computed fields
    private Long durationInMinutes;
    private Boolean isAvailable;
    private Boolean isVipSlot;
    private Boolean isMaintenanceSlot;
}
