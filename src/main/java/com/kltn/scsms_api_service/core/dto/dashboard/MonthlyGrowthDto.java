package com.kltn.scsms_api_service.core.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for monthly growth percentages
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyGrowthDto {
  private Double customers;
  private Double vehicles;
  private Double bookings;
  private Double revenue;
}
