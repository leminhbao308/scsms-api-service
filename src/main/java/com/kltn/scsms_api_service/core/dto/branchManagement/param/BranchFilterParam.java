package com.kltn.scsms_api_service.core.dto.branchManagement.param;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import com.kltn.scsms_api_service.core.entity.Branch;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BranchFilterParam extends BaseFilterParam<BranchFilterParam> {
    
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
    
    @JsonProperty("operating_status")
    private Branch.OperatingStatus operatingStatus;
    
    @JsonProperty("branch_type")
    private Branch.BranchType branchType;
    
    @JsonProperty("center_id")
    private UUID centerId;
    
    @JsonProperty("center_name")
    private String centerName;
    
    @JsonProperty("manager_id")
    private UUID managerId;
    
    @JsonProperty("manager_name")
    private String managerName;
    
    @JsonProperty("established_date_from")
    private LocalDate establishedDateFrom;
    
    @JsonProperty("established_date_to")
    private LocalDate establishedDateTo;
    
    @JsonProperty("min_service_capacity")
    private Integer minServiceCapacity;
    
    @JsonProperty("max_service_capacity")
    private Integer maxServiceCapacity;
    
    @JsonProperty("min_current_workload")
    private Integer minCurrentWorkload;
    
    @JsonProperty("max_current_workload")
    private Integer maxCurrentWorkload;
    
    @JsonProperty("min_employees")
    private Integer minEmployees;
    
    @JsonProperty("max_employees")
    private Integer maxEmployees;
    
    @JsonProperty("min_customers")
    private Integer minCustomers;
    
    @JsonProperty("max_customers")
    private Integer maxCustomers;
    
    @JsonProperty("min_monthly_revenue")
    private Double minMonthlyRevenue;
    
    @JsonProperty("max_monthly_revenue")
    private Double maxMonthlyRevenue;
    
    @JsonProperty("has_location")
    private Boolean hasLocation;
    
    @JsonProperty("has_manager")
    private Boolean hasManager;
    
    @JsonProperty("has_phone")
    private Boolean hasPhone;
    
    @JsonProperty("has_email")
    private Boolean hasEmail;
    
    @JsonProperty("is_at_capacity")
    private Boolean isAtCapacity;
    
    @JsonProperty("is_available")
    private Boolean isAvailable;
    
    // Location-based filtering
    @JsonProperty("latitude")
    private Double latitude;
    
    @JsonProperty("longitude")
    private Double longitude;
    
    @JsonProperty("radius_km")
    private Double radiusKm;
    
    @Override
    protected void standardizeSpecificFields(BranchFilterParam request) {
        // Standardize string fields
        request.setBranchName(trimAndNullify(request.getBranchName()));
        request.setBranchCode(trimAndNullify(request.getBranchCode()));
        request.setDescription(trimAndNullify(request.getDescription()));
        request.setAddress(trimAndNullify(request.getAddress()));
        request.setPhone(cleanPhoneNumber(request.getPhone()));
        request.setEmail(trimAndNullify(request.getEmail()));
        request.setCenterName(trimAndNullify(request.getCenterName()));
        request.setManagerName(trimAndNullify(request.getManagerName()));
    }
    
    @Override
    protected String getDefaultSortField() {
        return "branchName";
    }
}