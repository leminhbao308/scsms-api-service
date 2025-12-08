package com.kltn.scsms_api_service.core.dto.mediaManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaInfoDto {
    
    @JsonProperty("media_id")
    private UUID mediaId;
    
    @JsonProperty("entity_type")
    private String entityType;
    
    @JsonProperty("entity_id")
    private UUID entityId;
    
    @JsonProperty("media_url")
    private String mediaUrl;
    
    @JsonProperty("media_type")
    private String mediaType;
    
    @JsonProperty("is_main")
    private Boolean isMain;
    
    @JsonProperty("sort_order")
    private Integer sortOrder;
    
    @JsonProperty("alt_text")
    private String altText;
    
    // Audit fields
    @JsonProperty("created_date")
    private LocalDateTime createdDate;
    
    @JsonProperty("created_by")
    private String createdBy;
    
    @JsonProperty("is_deleted")
    private Boolean isDeleted;
}
