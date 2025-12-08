package com.kltn.scsms_api_service.core.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for product stock information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockDto {
  private String productId;
  private String productName;
  private Integer quantity;
  private Integer minStockLevel;
  private BigDecimal value;
  private String status; // LOW_STOCK, OUT_OF_STOCK, IN_STOCK
}
