package com.kltn.scsms_api_service.core.dto.productAttributeManagement.request;

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
public class UpdateProductAttributeStatusRequest {
    
    @NotNull(message = "Active status is required")
    @JsonProperty("is_active")
    private Boolean isActive;
}
