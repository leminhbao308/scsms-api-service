package com.kltn.scsms_api_service.core.dto.serviceManagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO cho thông tin pricing của từng bước trong Service Process
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessStepPricingDto {
    
    private UUID stepId;
    private String stepName;
    private String stepDescription;
    private Integer stepOrder;
    private Integer estimatedTime; // Thời gian dự kiến (phút)
    private Boolean isRequired;
    
    // Pricing information
    private BigDecimal stepTotalCost; // Tổng chi phí sản phẩm trong bước này
    private List<ProductPricingDto> products; // Danh sách sản phẩm trong bước
    private Integer productCount; // Số lượng sản phẩm trong bước
}
