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
    
    @JsonProperty("operating_hours")
    private String operatingHours;
    
    @JsonProperty("service_capacity")
    @Builder.Default
    private Integer serviceCapacity = 10;
    
    @JsonProperty("current_workload")
    @Builder.Default
    private Integer currentWorkload = 0;
    
    @JsonProperty("latitude")
    private Double latitude;
    
    @JsonProperty("longitude")
    private Double longitude;
    
    @JsonProperty("area_sqm")
    private Double areaSqm;
    
    @JsonProperty("parking_spaces")
    @Builder.Default
    private Integer parkingSpaces = 0;
    
    @JsonProperty("established_date")
    private LocalDate establishedDate;
    
    @JsonProperty("total_employees")
    @Builder.Default
    private Integer totalEmployees = 0;
    
    @JsonProperty("total_customers")
    @Builder.Default
    private Integer totalCustomers = 0;
    
    @JsonProperty("monthly_revenue")
    @Builder.Default
    private Double monthlyRevenue = 0.0;
    
    @JsonProperty("operating_status")
    private Branch.OperatingStatus operatingStatus;
    
    @JsonProperty("branch_type")
    private Branch.BranchType branchType;
    
    @JsonProperty("contact_info")
    private String contactInfo;
    
    @JsonProperty("facilities")
    private String facilities;
    
    @JsonProperty("services_offered")
    private String servicesOffered;
    
    // Center information
    @JsonProperty("center_id")
    private UUID centerId;
    
    @JsonProperty("center_name")
    private String centerName;
    
    @JsonProperty("center_code")
    private String centerCode;
    
    // Manager information
    @JsonProperty("manager_id")
    private UUID managerId;
    
    @JsonProperty("manager_name")
    private String managerName;
    
    @JsonProperty("manager_email")
    private String managerEmail;
    
    @JsonProperty("manager_assigned_at")
    private LocalDateTime managerAssignedAt;
    
    @JsonProperty("manager_assigned_by")
    private String managerAssignedBy;
    
    // Calculated fields
    @JsonProperty("utilization_rate")
    private Double utilizationRate;
    
    @JsonProperty("is_at_capacity")
    private Boolean isAtCapacity;
}