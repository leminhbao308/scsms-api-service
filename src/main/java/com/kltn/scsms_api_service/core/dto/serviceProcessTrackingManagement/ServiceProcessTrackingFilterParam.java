package com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement;

import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import com.kltn.scsms_api_service.core.entity.ServiceProcessTracking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Filter parameters cho ServiceProcessTracking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ServiceProcessTrackingFilterParam extends BaseFilterParam<ServiceProcessTrackingFilterParam> {
    
    // Filter by booking
    private UUID bookingId;
    private String bookingCode;
    
    // Filter by technician
    private UUID technicianId;
    private String technicianName;
    
    // Filter by slot
    private UUID slotId;
    private String slotName;
    
    // Filter by service step
    private UUID serviceStepId;
    private String serviceStepName;
    private Boolean isRequired;
    
    // Filter by status
    private ServiceProcessTracking.TrackingStatus status;
    
    // Filter by time range
    private LocalDateTime startDateFrom;
    private LocalDateTime startDateTo;
    private LocalDateTime endDateFrom;
    private LocalDateTime endDateTo;
    
    // Filter by progress
    private Double progressFrom;
    private Double progressTo;
    
    // Filter by duration
    private Integer estimatedDurationFrom;
    private Integer estimatedDurationTo;
    private Integer actualDurationFrom;
    private Integer actualDurationTo;
    
    // Filter by branch (through booking)
    private UUID branchId;
    private String branchName;
    
    // Filter by customer (through booking)
    private UUID customerId;
    private String customerName;
    private String customerPhone;
    
    // Filter by vehicle (through booking)
    private UUID vehicleId;
    private String vehicleLicensePlate;
    
    // Search text
    private String searchText;
}
