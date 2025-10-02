package com.kltn.scsms_api_service.core.dto.productTypeManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateProductTypeStatusRequest {
    
    @NotNull(message = "Active status is required")
    @JsonProperty("is_active")
    private Boolean isActive;
}
