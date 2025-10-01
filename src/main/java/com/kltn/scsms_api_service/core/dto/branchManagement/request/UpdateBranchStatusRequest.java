package com.kltn.scsms_api_service.core.dto.branchManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateBranchStatusRequest {
    
    @JsonProperty("is_active")
    private Boolean isActive;
}
