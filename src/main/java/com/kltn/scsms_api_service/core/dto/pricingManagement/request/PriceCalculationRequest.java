package com.kltn.scsms_api_service.core.dto.pricingManagement.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceCalculationRequest {
    private UUID customerId;
    private String customerTier;
    private String vehicleType;
    private List<PriceCalculationItem> items;
    private LocalDateTime calculationDate;
    private UUID branchId;
}
