package com.kltn.scsms_api_service.core.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for sales statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesStatsDto {
  private BigDecimal totalRevenue;
  private BigDecimal totalCost;
  private BigDecimal totalProfit;
  private Integer totalOrders;
  private Integer totalProducts;
  private BigDecimal averageOrderValue;
  private BigDecimal profitMargin;

  private List<DailySalesDto> dailySales;
  private List<TopProductDto> topProducts;
  private List<TopServiceDto> topServices;
}
