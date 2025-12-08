package com.kltn.scsms_api_service.core.dto.serviceTypeManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating ServiceType
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceTypeRequest {
    
    @NotBlank(message = "Service type code is required")
    @Size(max = 50, message = "Service type code must not exceed 50 characters")
    private String code;
    
    @NotBlank(message = "Service type name is required")
    @Size(max = 100, message = "Service type name must not exceed 100 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @JsonProperty("is_active")
    @Builder.Default
    private Boolean isActive = true;
}
