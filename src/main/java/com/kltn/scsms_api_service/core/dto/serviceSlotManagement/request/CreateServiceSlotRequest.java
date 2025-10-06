package com.kltn.scsms_api_service.core.dto.serviceSlotManagement.request;

import com.kltn.scsms_api_service.core.entity.ServiceSlot;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceSlotRequest {
    
    @NotNull(message = "Branch ID is required")
    private UUID branchId;
    
    @NotNull(message = "Slot date is required")
    private LocalDate slotDate;
    
    @NotNull(message = "Start time is required")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalTime endTime;
    
    @Builder.Default
    private ServiceSlot.SlotCategory slotCategory = ServiceSlot.SlotCategory.STANDARD;
    
    @Builder.Default
    private ServiceSlot.SlotStatus status = ServiceSlot.SlotStatus.AVAILABLE;
    
    @Builder.Default
    private Integer priorityOrder = 1;
    
    private String notes;
}
