package com.kltn.scsms_api_service.core.dto.productManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request DTO for updating product active status
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductStatusUpdateRequest {
    
    @NotNull(message = "Active status is required")
    @JsonProperty("is_active")
    private Boolean isActive;
}
