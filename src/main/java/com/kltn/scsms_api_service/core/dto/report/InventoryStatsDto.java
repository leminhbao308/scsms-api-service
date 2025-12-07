package com.kltn.scsms_api_service.core.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for inventory statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStatsDto {
  private Integer totalProducts;
  private Integer totalQuantity;
  private BigDecimal totalValue;
  private Integer lowStockCount;
  private Integer outOfStockCount;

  private List<ProductStockDto> lowStockProducts;
  private List<ProductStockDto> topValueProducts;
  private List<InventoryTransactionDto> recentTransactions;
}
