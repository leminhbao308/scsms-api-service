package com.kltn.scsms_api_service.core.dto.pricingManagement.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest {
    
    @NotNull(message = "Price list ID is required")
    private UUID priceListId;
    
    private String comment;
    
    private String reason;
}
