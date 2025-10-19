package com.kltn.scsms_api_service.core.dto.promotionManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.enumAttribute.SalesStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for promotion usage history
 * Contains information about each time a promotion was used in a sales order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionUsageHistoryDto {

  @JsonProperty("promotion_name")
  private String promotionName;

  @JsonProperty("promotion_code")
  private String promotionCode;

  @JsonProperty("customer_name")
  private String customerName;

  @JsonProperty("customer_phone")
  private String customerPhone;

  @JsonProperty("order_id")
  private UUID orderId;

  @JsonProperty("order_amount")
  private BigDecimal orderAmount; // Original amount before discount

  @JsonProperty("discount_amount")
  private BigDecimal discountAmount; // Total discount applied

  @JsonProperty("final_amount")
  private BigDecimal finalAmount; // Amount after discount

  @JsonProperty("used_date")
  private LocalDateTime usedDate; // When the order was created

  @JsonProperty("branch_name")
  private String branchName;

  @JsonProperty("status")
  private SalesStatus status; // Order status (FULFILLED, RETURNED, etc.)

  @JsonProperty("notes")
  private String notes; // Additional notes from promotion snapshot
}
