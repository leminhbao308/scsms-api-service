package com.kltn.scsms_api_service.core.dto.serviceSlotManagement.request;

import com.kltn.scsms_api_service.core.entity.ServiceSlot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceSlotRequest {
    
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private ServiceSlot.SlotCategory slotCategory;
    private ServiceSlot.SlotStatus status;
    private Integer priorityOrder;
    private String notes;
}
