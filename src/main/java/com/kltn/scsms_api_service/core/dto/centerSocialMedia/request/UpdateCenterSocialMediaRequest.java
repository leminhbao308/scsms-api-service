package com.kltn.scsms_api_service.core.dto.centerSocialMedia.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCenterSocialMediaRequest {
    
    @NotBlank(message = "Platform is required")
    private String platform;
    
    @NotBlank(message = "URL is required")
    private String url;
    
    private Boolean isActive;
}
