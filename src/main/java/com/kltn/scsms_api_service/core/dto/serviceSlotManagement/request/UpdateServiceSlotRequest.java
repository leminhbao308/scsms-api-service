package com.kltn.scsms_api_service.core.dto.serviceSlotManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    @JsonProperty("slot_date")
    private LocalDate slotDate;
    
    @JsonProperty("start_time")
    private LocalTime startTime;
    
    @JsonProperty("end_time")
    private LocalTime endTime;
    
    @JsonProperty("slot_category")
    private ServiceSlot.SlotCategory slotCategory;
    
    @JsonProperty("status")
    private ServiceSlot.SlotStatus status;
    
    @JsonProperty("priority_order")
    private Integer priorityOrder;
    
    @JsonProperty("notes")
    private String notes;
}
