package com.kltn.scsms_api_service.core.dto.promotionManagement.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.enumAttribute.SalesStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Filter parameters for promotion usage history
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionUsageHistoryFilterParam {

  // Pagination
  private Integer page = 0;
  private Integer size = 10;
  private String sort = "usedDate";
  private String direction = "DESC";

  // Filters
  @JsonProperty("promotion_code")
  private String promotionCode;

  @JsonProperty("promotion_name")
  private String promotionName;

  @JsonProperty("customer_name")
  private String customerName;

  @JsonProperty("customer_phone")
  private String customerPhone;

  @JsonProperty("order_id")
  private UUID orderId;

  @JsonProperty("branch_id")
  private UUID branchId;

  @JsonProperty("branch_name")
  private String branchName;

  @JsonProperty("status")
  private SalesStatus status;

  // Date range
  @JsonProperty("from_date")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime fromDate;

  @JsonProperty("to_date")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime toDate;

  /**
   * Standardize the filter parameters
   */
  public static PromotionUsageHistoryFilterParam standardize(PromotionUsageHistoryFilterParam param) {
    if (param == null) {
      param = new PromotionUsageHistoryFilterParam();
    }

    if (param.getPage() == null || param.getPage() < 0) {
      param.setPage(0);
    }

    if (param.getSize() == null || param.getSize() <= 0) {
      param.setSize(10);
    }

    if (param.getSort() == null || param.getSort().isBlank()) {
      param.setSort("usedDate");
    }

    if (param.getDirection() == null || param.getDirection().isBlank()) {
      param.setDirection("DESC");
    }

    return param;
  }
}
