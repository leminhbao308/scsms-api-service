package com.kltn.scsms_api_service.core.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for batch service pricing lookup
 * Used to get prices for multiple services in a single API call
 * Prevents N+1 query pattern on frontend
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchServicePricingRequest {

  /**
   * List of service IDs to get prices for
   */
  @JsonProperty("service_ids")
  private List<UUID> serviceIds;

  /**
   * Optional price book ID
   * If null, will use the active system price book
   */
  @JsonProperty("price_book_id")
  private UUID priceBookId;
}
