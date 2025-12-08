package com.kltn.scsms_api_service.core.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for top selling product
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDto {
  private String productId;
  private String productName;
  private Integer quantitySold;
  private BigDecimal revenue;
  private BigDecimal profit;
}
