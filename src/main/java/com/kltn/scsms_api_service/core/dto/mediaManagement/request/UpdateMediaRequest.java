package com.kltn.scsms_api_service.core.dto.mediaManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateMediaRequest {
    
    @JsonProperty("media_url")
    private String mediaUrl;
    
    @JsonProperty("media_type")
    private String mediaType;
    
    @JsonProperty("sort_order")
    private Integer sortOrder;
    
    @Size(max = 255, message = "Alt text must not exceed 255 characters")
    @JsonProperty("alt_text")
    private String altText;
}
