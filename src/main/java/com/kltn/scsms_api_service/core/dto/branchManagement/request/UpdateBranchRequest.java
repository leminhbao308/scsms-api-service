package com.kltn.scsms_api_service.core.dto.branchManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.Branch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateBranchRequest {
    
    @Size(min = 2, max = 255, message = "Branch name must be between 2 and 255 characters")
    @JsonProperty("branch_name")
    private String branchName;
    
    @Size(min = 2, max = 50, message = "Branch code must be between 2 and 50 characters")
    @JsonProperty("branch_code")
    private String branchCode;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @JsonProperty("description")
    private String description;
    
    @Size(max = 1000, message = "Address must not exceed 1000 characters")
    @JsonProperty("address")
    private String address;
    
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @JsonProperty("phone")
    private String phone;
    
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("operating_hours")
    private String operatingHours;
    
    @JsonProperty("service_capacity")
    private Integer serviceCapacity;
    
    @JsonProperty("current_workload")
    private Integer currentWorkload;
    
    @JsonProperty("latitude")
    private Double latitude;
    
    @JsonProperty("longitude")
    private Double longitude;
    
    @JsonProperty("area_sqm")
    private Double areaSqm;
    
    @JsonProperty("parking_spaces")
    private Integer parkingSpaces;
    
    @JsonProperty("established_date")
    private LocalDate establishedDate;
    
    @JsonProperty("center_id")
    private UUID centerId;
    
    @JsonProperty("manager_id")
    private UUID managerId;
    
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
    
    @JsonProperty("is_active")
    private Boolean isActive;
}