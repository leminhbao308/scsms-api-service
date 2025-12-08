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
    
    
    @JsonProperty("center_id")
    private UUID centerId;
    
    
    @JsonProperty("manager_id")
    private UUID managerId;
    
    
    @JsonProperty("established_date_from")
    private LocalDate establishedDateFrom;
    
    @JsonProperty("established_date_to")
    private LocalDate establishedDateTo;
    
    @JsonProperty("min_service_capacity")
    private Integer minServiceCapacity;
    
    @JsonProperty("max_service_capacity")
    private Integer maxServiceCapacity;
    
    
    
    
    @JsonProperty("has_manager")
    private Boolean hasManager;
    
    @JsonProperty("has_phone")
    private Boolean hasPhone;
    
    @JsonProperty("has_email")
    private Boolean hasEmail;
    
    
    
    @Override
    protected void standardizeSpecificFields(BranchFilterParam request) {
        // Standardize string fields
        request.setBranchName(trimAndNullify(request.getBranchName()));
        request.setBranchCode(trimAndNullify(request.getBranchCode()));
        request.setDescription(trimAndNullify(request.getDescription()));
        request.setAddress(trimAndNullify(request.getAddress()));
        request.setPhone(cleanPhoneNumber(request.getPhone()));
        request.setEmail(trimAndNullify(request.getEmail()));
    }
    
    @Override
    protected String getDefaultSortField() {
        return "branchName";
    }
}