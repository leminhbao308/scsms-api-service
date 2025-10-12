package com.kltn.scsms_api_service.core.dto.serviceProcessManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateServiceProcessRequest {
    
    @NotBlank(message = "Process code is required")
    @Size(min = 2, max = 50, message = "Process code must be between 2 and 50 characters")
    @JsonProperty("code")
    private String code;
    
    @NotBlank(message = "Process name is required")
    @Size(min = 2, max = 150, message = "Process name must be between 2 and 150 characters")
    @JsonProperty("name")
    private String name;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @JsonProperty("description")
    private String description;
    
    @Min(value = 0, message = "Estimated duration must be at least 0 minutes")
    @JsonProperty("estimated_duration")
    private Integer estimatedDuration;
    
    @JsonProperty("is_default")
    private Boolean isDefault;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @Valid
    @JsonProperty("process_steps")
    private List<CreateServiceProcessStepRequest> processSteps;
}
