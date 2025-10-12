package com.kltn.scsms_api_service.core.dto.serviceProcessManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ServiceProcessStepProductInfoDto {
    
    private UUID id;
    
    @JsonProperty("step_id")
    private UUID stepId;
    
    @JsonProperty("step_name")
    private String stepName;
    
    @JsonProperty("product_id")
    private UUID productId;
    
    @JsonProperty("product_name")
    private String productName;
    
    @JsonProperty("product_code")
    private String productCode;
    
    @JsonProperty("product_sku")
    private String productSku;
    
    private BigDecimal quantity;
    private String unit;
    
    @JsonProperty("product_cost")
    private BigDecimal productCost;
    
    @Builder.Default
    private AuditDto audit = AuditDto.builder().build();
}
