package com.kltn.scsms_api_service.core.dto.serviceSlotManagement.param;

import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import com.kltn.scsms_api_service.core.entity.ServiceSlot;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceSlotFilterParam extends BaseFilterParam<ServiceSlotFilterParam> {
    
    private UUID branchId;
    private LocalDate slotDate;
    private LocalDate slotDateFrom;
    private LocalDate slotDateTo;
    private ServiceSlot.SlotCategory slotCategory;
    private ServiceSlot.SlotStatus status;
    private Integer priorityOrder;
    private Boolean isActive;
    private Boolean isDeleted;
    
}
