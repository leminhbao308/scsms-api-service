package com.kltn.scsms_api_service.core.dto.serviceProcessManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateServiceProcessStepRequest {
    
    @NotNull(message = "Step order is required")
    @Min(value = 1, message = "Step order must be at least 1")
    @JsonProperty("step_order")
    private Integer stepOrder;
    
    @NotBlank(message = "Step name is required")
    @Size(min = 2, max = 150, message = "Step name must be between 2 and 150 characters")
    @JsonProperty("name")
    private String name;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @JsonProperty("description")
    private String description;
    
    @Min(value = 1, message = "Estimated time must be at least 1 minute")
    @JsonProperty("estimated_time")
    private Integer estimatedTime;
    
    @JsonProperty("is_required")
    private Boolean isRequired;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
}
