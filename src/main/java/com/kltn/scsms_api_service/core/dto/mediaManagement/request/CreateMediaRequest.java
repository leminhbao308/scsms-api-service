package com.kltn.scsms_api_service.core.dto.mediaManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateMediaRequest {
    
    @NotNull(message = "Entity type is required")
    @JsonProperty("entity_type")
    private String entityType;
    
    @NotNull(message = "Entity ID is required")
    @JsonProperty("entity_id")
    private UUID entityId;
    
    @NotBlank(message = "Media URL is required")
    @JsonProperty("media_url")
    private String mediaUrl;
    
    @JsonProperty("media_type")
    @Builder.Default
    private String mediaType = "IMAGE";
    
    @JsonProperty("is_main")
    @Builder.Default
    private Boolean isMain = false;
    
    @JsonProperty("sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
    
    @Size(max = 255, message = "Alt text must not exceed 255 characters")
    @JsonProperty("alt_text")
    private String altText;
}
