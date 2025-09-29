package com.kltn.scsms_api_service.core.dto.inventoryManagement.response;

import com.kltn.scsms_api_service.core.entity.enumAttribute.QualityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDetailResponse {
    private UUID inventoryDetailId;
    private UUID inventoryId;
    private UUID productId;
    private String productName;
    private String productSku;
    private String unitOfMeasure;
    private Integer quantity;
    private LocalDate productionDate;
    private Integer receivedQuantity;
    private Integer rejectedQuantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private BigDecimal finalAmount;
    private String batchNumber;
    private List<String> serialNumbers;
    private QualityStatus qualityStatus;
    private String notes;
}
