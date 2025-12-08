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
public class InventoryLotDTO {

  @JsonProperty("lot_id")
  private UUID lotId;

  @JsonProperty("lot_code")
  private String lotCode;

  @JsonProperty("product_id")
  private UUID productId;

  @JsonProperty("product_name")
  private String productName;

  @JsonProperty("product_sku")
  private String productSku;

  @JsonProperty("supplier_id")
  private UUID supplierId;

  @JsonProperty("supplier_name")
  private String supplierName;

  @JsonProperty("branch_id")
  private UUID branchId;

  @JsonProperty("branch_name")
  private String branchName;

  @JsonProperty("received_at")
  private LocalDateTime receivedAt;

  @JsonProperty("expiry_date")
  private LocalDateTime expiryDate;

  @JsonProperty("unit_cost")
  private BigDecimal unitCost;

  @JsonProperty("qty_received")
  private Long qtyReceived; // Số lượng nhập ban đầu

  @JsonProperty("qty_current")
  private Long qtyCurrent; // Số lượng hiện tại còn lại trong lô

  @JsonProperty("qty_sold")
  private Long qtySold; // Số lượng đã bán

  @JsonProperty("qty_reserved")
  private Long qtyReserved; // Số lượng đang được đặt trước

  @JsonProperty("qty_available")
  private Long qtyAvailable; // Số lượng khả dụng = qtyCurrent - qtyReserved

  @JsonProperty("status")
  private String status; // ACTIVE, DEPLETED, EXPIRED

  @JsonProperty("created_date")
  private LocalDateTime createdDate;

  @JsonProperty("created_by")
  private String createdBy;
}
