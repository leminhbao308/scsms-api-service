package com.kltn.scsms_api_service.core.dto.serviceProcessManagement.param;

import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ServiceProcessFilterParam extends BaseFilterParam<ServiceProcessFilterParam> {
    
    private String code;
    private String name;
    private Boolean isDefault;
    private Boolean isActive;
    private Boolean hasSteps;
    private Integer minEstimatedDuration;
    private Integer maxEstimatedDuration;
}
