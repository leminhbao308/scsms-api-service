package com.kltn.scsms_api_service.core.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for top service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopServiceDto {
  private String serviceId;
  private String serviceName;
  private Integer bookingCount;
  private BigDecimal revenue;
}
