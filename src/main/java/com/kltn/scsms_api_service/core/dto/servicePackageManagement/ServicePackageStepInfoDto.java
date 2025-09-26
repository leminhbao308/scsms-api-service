package com.kltn.scsms_api_service.core.dto.servicePackageManagement;

import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import com.kltn.scsms_api_service.core.entity.ServicePackageStep;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicePackageStepInfoDto {
    
    private UUID packageStepId;
    private UUID packageId;
    private String packageName;
    private String stepName;
    private String description;
    private Integer stepOrder;
    private ServicePackageStep.StepType stepType;
    private UUID referencedServiceId;
    private String referencedServiceName;
    private Integer estimatedDuration;
    private String instructions;
    private Boolean isOptional;
    private Boolean isParallel;
    private String prerequisiteSteps;
    private String requiredSkillLevel;
    private String requiredTools;
    private String safetyNotes;
    private String qualityCriteria;
    private Boolean photoRequired;
    private Boolean customerApprovalRequired;
    private Boolean isActive;
    private Boolean isDeleted;
    private AuditDto audit;
}