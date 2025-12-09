package com.kltn.scsms_api_service.core.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for revenue statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatsDto {
  private BigDecimal totalRevenue;
  private BigDecimal monthlyRevenue;
  private BigDecimal weeklyRevenue;
  private BigDecimal dailyRevenue;

  private List<MonthlyRevenueDto> monthlyRevenueData;
}
