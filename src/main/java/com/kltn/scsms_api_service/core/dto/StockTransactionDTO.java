package com.kltn.scsms_api_service.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransactionDTO {

  @JsonProperty("id")
  private UUID id;

  @JsonProperty("type")
  private String type; // PURCHASE_RECEIPT, SALE, RETURN, RESERVATION, RELEASE, ADJUSTMENT

  @JsonProperty("quantity")
  private Long quantity;

  @JsonProperty("unit_cost")
  private BigDecimal unitCost;

  @JsonProperty("lot_code")
  private String lotCode;

  @JsonProperty("lot_id")
  private UUID lotId;

  @JsonProperty("ref_type")
  private String refType; // SALE_ORDER, PURCHASE_ORDER, etc.

  @JsonProperty("ref_id")
  private UUID refId;

  @JsonProperty("branch_id")
  private UUID branchId;

  @JsonProperty("branch_name")
  private String branchName;

  @JsonProperty("product_id")
  private UUID productId;

  @JsonProperty("product_name")
  private String productName;

  @JsonProperty("product_sku")
  private String productSku;

  @JsonProperty("supplier_name")
  private String supplierName;

  @JsonProperty("created_date")
  private LocalDateTime createdDate;

  @JsonProperty("created_by")
  private String createdBy;

  @JsonProperty("modified_date")
  private LocalDateTime modifiedDate;

  @JsonProperty("modified_by")
  private String modifiedBy;
}
