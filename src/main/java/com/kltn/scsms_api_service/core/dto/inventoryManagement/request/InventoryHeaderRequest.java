package com.kltn.scsms_api_service.core.dto.inventoryManagement.request;

import com.kltn.scsms_api_service.core.entity.enumAttribute.InventoryStatus;
import com.kltn.scsms_api_service.core.entity.enumAttribute.ReferenceType;
import com.kltn.scsms_api_service.core.entity.enumAttribute.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryHeaderRequest {
    private String inventoryCode;
    private TransactionType transactionType;
    private UUID branchId;
    private UUID supplierId;
    private ReferenceType referenceType;
    private UUID referenceId;
    private String referenceCode;
    private LocalDateTime transactionDate;
    private LocalDateTime expectedReceiveDate;
    private LocalDateTime expectedShipDate;
    private InventoryStatus status;
    private BigDecimal shippingCost;
    private BigDecimal otherCosts;
    private String notes;
    private String requestedBy;
    private String shippingInfo;
    private String paymentInfo;
    private List<InventoryDetailRequest> details;
}
