package com.kltn.scsms_api_service.core.dto.productManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for reordering product images
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderProductImagesRequest {

  /**
   * List of media items with their new sort orders
   */
  @JsonProperty("media_orders")
  @NotEmpty(message = "Media orders cannot be empty")
  private List<MediaOrderDto> mediaOrders;

  /**
   * DTO for media order
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MediaOrderDto {

    @JsonProperty("media_id")
    @NotNull(message = "Media ID is required")
    private UUID mediaId;

    @JsonProperty("sort_order")
    @NotNull(message = "Sort order is required")
    private Integer sortOrder;
  }
}
