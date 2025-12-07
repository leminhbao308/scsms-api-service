package com.kltn.scsms_api_service.core.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for monthly revenue data point
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRevenueDto {
  private String month;
  private BigDecimal revenue;
  private Long orderCount;
}
