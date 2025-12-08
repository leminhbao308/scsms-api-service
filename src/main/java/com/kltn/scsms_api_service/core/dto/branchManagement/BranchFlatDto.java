package com.kltn.scsms_api_service.core.dto.branchManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.Branch;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BranchFlatDto {
    
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
    
    /**
     * Số lượng khu vực dịch vụ cố định cho mỗi chi nhánh
     * UI chỉ hiển thị, không cho phép thay đổi
     */
    @JsonProperty("service_slots")
    @Builder.Default
    private Integer serviceSlots = 8;
    
    @JsonProperty("established_date")
    private LocalDate establishedDate;
    
    @JsonProperty("operating_status")
    private Branch.OperatingStatus operatingStatus;
    
    @JsonProperty("center_id")
    private UUID centerId;
    
    @JsonProperty("manager_id")
    private UUID managerId;
}
