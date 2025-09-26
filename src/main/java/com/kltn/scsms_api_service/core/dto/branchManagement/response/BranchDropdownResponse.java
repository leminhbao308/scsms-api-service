package com.kltn.scsms_api_service.core.dto.branchManagement.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class BranchDropdownResponse {
    
    @JsonProperty("branch_id")
    private UUID branchId;
    
    @JsonProperty("branch_name")
    private String branchName;
    
    @JsonProperty("address")
    private String address;
    
    @JsonProperty("service_capacity")
    private Integer serviceCapacity;
    
    @JsonProperty("is_active")
    private Boolean isActive;
}
