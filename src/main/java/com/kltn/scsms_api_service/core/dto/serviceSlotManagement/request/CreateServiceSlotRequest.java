package com.kltn.scsms_api_service.core.dto.serviceSlotManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("branch_id")
    private UUID branchId;
    
    @NotNull(message = "Slot date is required")
    @JsonProperty("slot_date")
    private LocalDate slotDate;
    
    @NotNull(message = "Start time is required")
    @JsonProperty("start_time")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    @JsonProperty("end_time")
    private LocalTime endTime;
    
    @Builder.Default
    @JsonProperty("slot_category")
    private ServiceSlot.SlotCategory slotCategory = ServiceSlot.SlotCategory.STANDARD;
    
    @Builder.Default
    @JsonProperty("status")
    private ServiceSlot.SlotStatus status = ServiceSlot.SlotStatus.AVAILABLE;
    
    @Builder.Default
    @JsonProperty("priority_order")
    private Integer priorityOrder = 1;
    
    @JsonProperty("notes")
    private String notes;
}
