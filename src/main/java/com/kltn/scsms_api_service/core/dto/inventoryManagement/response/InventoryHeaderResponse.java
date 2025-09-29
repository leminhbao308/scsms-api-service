package com.kltn.scsms_api_service.core.dto.inventoryManagement.response;

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
public class InventoryHeaderResponse {
    private UUID inventoryId;
    private String inventoryCode;
    private TransactionType transactionType;
    private UUID branchId;
    private String branchName;
    private UUID supplierId;
    private String supplierName;
    private ReferenceType referenceType;
    private UUID referenceId;
    private String referenceCode;
    private LocalDateTime transactionDate;
    private LocalDateTime expectedReceiveDate;
    private LocalDateTime actualReceiveDate;
    private LocalDateTime expectedShipDate;
    private LocalDateTime actualShipDate;
    private InventoryStatus status;
    private Integer totalItems;
    private Integer totalQuantity;
    private BigDecimal totalAmount;
    private BigDecimal shippingCost;
    private BigDecimal otherCosts;
    private BigDecimal grandTotal;
    private String notes;
    private String requestedBy;
    private String approvedBy;
    private LocalDateTime approvedDate;
    private String processedBy;
    private LocalDateTime processedDate;
    private String shippingInfo;
    private String paymentInfo;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private List<InventoryDetailResponse> details;
}
