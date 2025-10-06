package com.kltn.scsms_api_service.core.dto.serviceProcessManagement;

import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceProcessStepProductInfoDto {
    
    private UUID id;
    private UUID stepId;
    private String stepName;
    private UUID productId;
    private String productName;
    private String productCode;
    private String productSku;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal productCost;
    private AuditDto audit;
}
