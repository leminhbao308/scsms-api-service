package com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelImportPreviewResponseDto {
  @JsonProperty("total_rows")
  private Integer totalRows;

  @JsonProperty("valid_rows")
  private Integer validRows;

  @JsonProperty("invalid_rows")
  private Integer invalidRows;

  private List<ExcelImportErrorDto> errors;

  @JsonProperty("preview_data")
  private List<ExcelImportPreviewRowDto> previewData;
}
