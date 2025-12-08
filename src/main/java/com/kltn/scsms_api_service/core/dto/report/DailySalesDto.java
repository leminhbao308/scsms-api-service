package com.kltn.scsms_api_service.core.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for daily sales data point
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySalesDto {
  private String date;
  private BigDecimal revenue;
  private BigDecimal profit;
  private Integer orderCount;
}
