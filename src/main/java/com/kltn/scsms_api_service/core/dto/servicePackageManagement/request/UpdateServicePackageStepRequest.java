package com.kltn.scsms_api_service.core.dto.servicePackageManagement.request;

import com.kltn.scsms_api_service.core.entity.ServicePackageStep;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class UpdateServicePackageStepRequest {

    private UUID packageStepId;

    @Size(max = 255, message = "Step name must not exceed 255 characters")
    private String stepName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Min(value = 1, message = "Estimated duration must be at least 1 minute")
    private Integer estimatedDuration;

    private Boolean isOptional;

    @Size(max = 500, message = "Instructions must not exceed 500 characters")
    private String instructions;

    private ServicePackageStep.StepType stepType;

    private UUID referencedServiceId;

    private Integer stepOrder;
    
    // Manual getter for packageStepId to ensure it's available
    public UUID getPackageStepId() {
        return packageStepId;
    }
    
    public void setPackageStepId(UUID packageStepId) {
        this.packageStepId = packageStepId;
    }
}