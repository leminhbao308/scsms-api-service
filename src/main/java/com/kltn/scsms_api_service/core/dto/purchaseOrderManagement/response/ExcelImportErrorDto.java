package com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelImportErrorDto {
  private Integer row;
  private String field;
  private String message;
}
