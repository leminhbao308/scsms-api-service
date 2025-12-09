package com.kltn.scsms_api_service.core.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for overall dashboard statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
  private Long totalCustomers;
  private Long totalVehicles;
  private Long totalBookings;
  private BigDecimal totalRevenue;

  private MonthlyGrowthDto monthlyGrowth;
}
