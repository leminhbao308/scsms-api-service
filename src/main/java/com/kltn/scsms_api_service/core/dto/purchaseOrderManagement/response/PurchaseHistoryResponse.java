package com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.PurchaseOrderLineInfoDto;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseHistoryResponse {
    
    @JsonProperty("peak_unit_cost")
    private BigDecimal peakUnitCost;
    
    private List<PurchaseOrderLineInfoDto> lines;
}
