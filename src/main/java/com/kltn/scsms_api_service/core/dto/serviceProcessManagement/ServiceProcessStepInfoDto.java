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
public class ServiceProcessStepInfoDto {
    
    private UUID id;
    private UUID processId;
    private String processName;
    private Integer stepOrder;
    private String name;
    private String description;
    private Integer estimatedTime;
    private Boolean isRequired;
    private Boolean isFirstStep;
    private Boolean isLastStep;
    private Integer totalProductCount;
    private List<ServiceProcessStepProductInfoDto> stepProducts;
    private AuditDto audit;
}
