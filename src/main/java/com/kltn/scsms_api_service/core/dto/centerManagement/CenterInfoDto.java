package com.kltn.scsms_api_service.core.dto.centerManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import com.kltn.scsms_api_service.core.entity.Center;
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
public class CenterInfoDto extends AuditDto {
    
    @JsonProperty("center_id")
    private UUID centerId;
    
    @JsonProperty("center_name")
    private String centerName;
    
    @JsonProperty("center_code")
    private String centerCode;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("headquarters_address")
    private String headquartersAddress;
    
    @JsonProperty("headquarters_phone")
    private String headquartersPhone;
    
    @JsonProperty("headquarters_email")
    private String headquartersEmail;
    
    @JsonProperty("website")
    private String website;
    
    @JsonProperty("tax_code")
    private String taxCode;
    
    @JsonProperty("business_license")
    private String businessLicense;
    
    @JsonProperty("logo_url")
    private String logoUrl;
    
    @JsonProperty("established_date")
    private LocalDate establishedDate;
    
    @JsonProperty("total_branches")
    @Builder.Default
    private Integer totalBranches = 0;
    
    @JsonProperty("total_employees")
    @Builder.Default
    private Integer totalEmployees = 0;
    
    @JsonProperty("total_customers")
    @Builder.Default
    private Integer totalCustomers = 0;
    
    @JsonProperty("operating_status")
    private Center.OperatingStatus operatingStatus;
    
    @JsonProperty("business_hours")
    private String businessHours;
    
    @JsonProperty("contact_info")
    private String contactInfo;
    
    @JsonProperty("social_media")
    private String socialMedia;
    
    @JsonProperty("service_areas")
    private String serviceAreas;
    
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
}
