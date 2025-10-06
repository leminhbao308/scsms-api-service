package com.kltn.scsms_api_service.core.dto.serviceProcessManagement;

import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceProcessInfoDto {
    
    private UUID id;
    private String code;
    private String name;
    private String description;
    private Integer estimatedDuration;
    private Boolean isDefault;
    private Boolean isActive;
    private Integer stepCount;
    private List<ServiceProcessStepInfoDto> processSteps;
    private AuditDto audit;
}
