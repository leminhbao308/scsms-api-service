package com.kltn.scsms_api_service.core.dto.centerSocialMedia;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CenterSocialMediaDto {
    
    private UUID socialMediaId;
    private UUID centerId;
    private String platform;
    private String url;
    private Boolean isActive;
    private String createdBy;
    private String modifiedBy;
    private java.time.LocalDateTime createdDate;
    private java.time.LocalDateTime modifiedDate;
}
