package com.kltn.scsms_api_service.core.dto.branchManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import com.kltn.scsms_api_service.core.entity.Branch;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BranchInfoDto extends AuditDto {
    
    @JsonProperty("branch_id")
    private UUID branchId;
    
    @JsonProperty("branch_name")
    private String branchName;
    
    @JsonProperty("branch_code")
    private String branchCode;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("address")
    private String address;
    
    @JsonProperty("phone")
    private String phone;
    
    @JsonProperty("email")
    private String email;
    
    
    @JsonProperty("service_capacity")
    @Builder.Default
    private Integer serviceCapacity = 10;
    
    
    
    @JsonProperty("area_sqm")
    private Double areaSqm;
    
    @JsonProperty("parking_spaces")
    @Builder.Default
    private Integer parkingSpaces = 0;
    
    @JsonProperty("established_date")
    private LocalDate establishedDate;
    
    
    @JsonProperty("operating_status")
    private Branch.OperatingStatus operatingStatus;
    
    
    
    // Center information
    @JsonProperty("center_id")
    private UUID centerId;
    
    
    // Manager information
    @JsonProperty("manager_id")
    private UUID managerId;
    
    
    @JsonProperty("manager_assigned_at")
    private LocalDateTime managerAssignedAt;
    
    @JsonProperty("manager_assigned_by")
    private String managerAssignedBy;
    
}