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
public class CreateServicePackageStepRequest {

    @NotBlank(message = "Step name is required")
    @Size(max = 255, message = "Step name must not exceed 255 characters")
    private String stepName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Min(value = 1, message = "Estimated duration must be at least 1 minute")
    private Integer estimatedDuration;

    @NotNull(message = "Is optional flag is required")
    private Boolean isOptional;

    @Size(max = 500, message = "Instructions must not exceed 500 characters")
    private String instructions;

    private ServicePackageStep.StepType stepType;

    private UUID referencedServiceId;

    private Integer stepOrder;
}