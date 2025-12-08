package com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelImportPreviewRowDto {
  @JsonProperty("product_code")
  private String productCode;

  @JsonProperty("product_name")
  private String productName;

  @JsonProperty("supplier_name")
  private String supplierName;

  private Long quantity;

  @JsonProperty("unit_cost")
  private BigDecimal unitCost;

  @JsonProperty("lot_code")
  private String lotCode;

  @JsonProperty("expiry_date")
  private LocalDateTime expiryDate;
}
