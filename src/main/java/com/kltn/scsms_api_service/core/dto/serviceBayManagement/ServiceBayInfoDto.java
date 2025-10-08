package com.kltn.scsms_api_service.core.dto.serviceBayManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    @JsonProperty("bay_id")
    private UUID bayId;
    
    @JsonProperty("branch_id")
    private UUID branchId;
    
    @JsonProperty("branch_name")
    private String branchName;
    
    @JsonProperty("branch_code")
    private String branchCode;
    
    @JsonProperty("bay_name")
    private String bayName;
    
    @JsonProperty("bay_code")
    private String bayCode;
    
    @JsonProperty("bay_type")
    private ServiceBay.BayType bayType;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("capacity")
    private Integer capacity;
    
    @JsonProperty("status")
    private ServiceBay.BayStatus status;
    
    @JsonProperty("display_order")
    private Integer displayOrder;
    
    @JsonProperty("notes")
    private String notes;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("is_deleted")
    private Boolean isDeleted;
    
    // Computed fields
    @JsonProperty("is_available")
    private Boolean isAvailable;
    
    @JsonProperty("is_maintenance")
    private Boolean isMaintenance;
    
    @JsonProperty("is_closed")
    private Boolean isClosed;
    
    @JsonProperty("is_wash_bay")
    private Boolean isWashBay;
    
    @JsonProperty("is_repair_bay")
    private Boolean isRepairBay;
    
    @JsonProperty("is_lift_bay")
    private Boolean isLiftBay;
    
    @JsonProperty("total_bookings")
    private Long totalBookings;
    
    @JsonProperty("active_bookings")
    private Long activeBookings;
}
