package com.kltn.scsms_api_service.core.dto.centerSocialMedia.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCenterSocialMediaRequest {
    
    @NotNull(message = "Center ID is required")
    private UUID centerId;
    
    @NotBlank(message = "Platform is required")
    private String platform;
    
    @NotBlank(message = "URL is required")
    private String url;
    
    @Builder.Default
    private Boolean isActive = true;
}
