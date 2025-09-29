package com.kltn.scsms_api_service.core.dto.pricingManagement.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceCalculationResponse {
    private List<PriceCalculationResult> itemResults;
    private List<ComboSuggestion> comboSuggestions;
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal finalTotal;
    private String currency;
    private LocalDateTime calculatedAt;
}
