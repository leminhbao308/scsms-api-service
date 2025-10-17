package com.kltn.scsms_api_service.core.dto.serviceProcessManagement.param;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    @JsonProperty("is_default")
    private Boolean isDefault;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("has_steps")
    private Boolean hasSteps;
    
    // Loại bỏ min/max estimated duration - thời gian được quản lý ở Service level
}
