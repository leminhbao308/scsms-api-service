package com.kltn.scsms_api_service.core.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO containing service pricing information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicePriceDto {

  /**
   * Service ID
   */
  @JsonProperty("service_id")
  private UUID serviceId;

  /**
   * Service name for reference
   */
  @JsonProperty("service_name")
  private String serviceName;

  /**
   * Fixed price from price book
   */
  @JsonProperty("fixed_price")
  private BigDecimal fixedPrice;

  /**
   * Whether the service was found in the price book
   */
  @JsonProperty("found")
  private boolean found;
}
