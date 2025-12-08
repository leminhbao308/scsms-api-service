package com.kltn.scsms_api_service.core.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for inventory transaction (inbound/outbound)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionDto {
  private UUID id;
  private String transactionType; // "INBOUND" (nhập kho) or "OUTBOUND" (xuất kho)
  private String productName;
  private String productSku;
  private String branchName;
  private Long quantity;
  private BigDecimal unitPrice;
  private BigDecimal totalValue;
  private LocalDateTime transactionDate;
  private String referenceType; // "PURCHASE_ORDER", "SALES_ORDER", "ADJUSTMENT"
  private String referenceCode;
}
