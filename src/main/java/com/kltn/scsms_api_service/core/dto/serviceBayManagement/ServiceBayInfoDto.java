package com.kltn.scsms_api_service.core.dto.serviceBayManagement;

import com.kltn.scsms_api_service.core.entity.ServiceBay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO chứa thông tin chi tiết của service bay
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBayInfoDto {
    
    private UUID bayId;
    private UUID branchId;
    private String branchName;
    private String branchCode;
    private String bayName;
    private String bayCode;
    private ServiceBay.BayType bayType;
    private String description;
    private Integer capacity;
    private ServiceBay.BayStatus status;
    private Integer displayOrder;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
    private Boolean isDeleted;
    
    // Computed fields
    private Boolean isAvailable;
    private Boolean isMaintenance;
    private Boolean isClosed;
    private Boolean isWashBay;
    private Boolean isRepairBay;
    private Boolean isLiftBay;
    private Long totalBookings;
    private Long activeBookings;
}
