package com.kltn.scsms_api_service.core.dto.serviceProcessManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ServiceProcessInfoDto {
    
    private UUID id;
    private String code;
    private String name;
    private String description;
    
    @JsonProperty("estimated_duration")
    private Integer estimatedDuration;
    
    @JsonProperty("is_default")
    private Boolean isDefault;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("step_count")
    private Integer stepCount;
    
    @JsonProperty("process_steps")
    private List<ServiceProcessStepInfoDto> processSteps;
    
    @Builder.Default
    private AuditDto audit = AuditDto.builder().build();
}
