package com.kltn.scsms_api_service.core.dto.mediaManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkUpdateMediaOrderRequest {
    
    @NotNull(message = "Entity type is required")
    @JsonProperty("entity_type")
    private String entityType;
    
    @NotNull(message = "Entity ID is required")
    @JsonProperty("entity_id")
    private String entityId;
    
    @NotEmpty(message = "Media orders list cannot be empty")
    @Valid
    @JsonProperty("media_orders")
    private List<MediaOrderDto> mediaOrders;
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaOrderDto {
        
        @NotNull(message = "Media ID is required")
        @JsonProperty("media_id")
        private String mediaId;
        
        @NotNull(message = "Sort order is required")
        @JsonProperty("sort_order")
        private Integer sortOrder;
    }
}
