package com.kltn.scsms_api_service.core.dto.productManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adding/updating product images
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageRequest {

  /**
   * URL of the image (from S3 or CDN)
   */
  @JsonProperty("media_url")
  @NotBlank(message = "Media URL is required")
  private String mediaUrl;

  /**
   * Alternative text for the image (for SEO and accessibility)
   */
  @JsonProperty("alt_text")
  private String altText;

  /**
   * Whether this image should be set as the main image
   */
  @JsonProperty("is_main")
  @Builder.Default
  private Boolean isMain = false;

  /**
   * Sort order of the image
   */
  @JsonProperty("sort_order")
  @Builder.Default
  private Integer sortOrder = 0;
}
