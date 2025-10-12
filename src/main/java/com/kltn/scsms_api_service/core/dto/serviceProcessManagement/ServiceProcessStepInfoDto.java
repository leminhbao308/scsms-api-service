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
public class ServiceProcessStepInfoDto {
    
    private UUID id;
    
    @JsonProperty("process_id")
    private UUID processId;
    
    @JsonProperty("process_name")
    private String processName;
    
    @JsonProperty("step_order")
    private Integer stepOrder;
    
    private String name;
    private String description;
    
    @JsonProperty("estimated_time")
    private Integer estimatedTime;
    
    @JsonProperty("is_required")
    private Boolean isRequired;
    
    @JsonProperty("is_first_step")
    private Boolean isFirstStep;
    
    @JsonProperty("is_last_step")
    private Boolean isLastStep;
    
    @JsonProperty("total_product_count")
    private Integer totalProductCount;
    
    @JsonProperty("step_products")
    private List<ServiceProcessStepProductInfoDto> stepProducts;
    
    @Builder.Default
    private AuditDto audit = AuditDto.builder().build();
}
