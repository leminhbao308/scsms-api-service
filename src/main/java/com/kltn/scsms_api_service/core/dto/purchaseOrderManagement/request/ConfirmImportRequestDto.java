package com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmImportRequestDto {
  @JsonProperty("branch_id")
  private UUID branchId;

  private List<ExcelImportLineDto> lines;
}
