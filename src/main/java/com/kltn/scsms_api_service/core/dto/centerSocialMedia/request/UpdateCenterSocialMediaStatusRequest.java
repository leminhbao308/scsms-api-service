package com.kltn.scsms_api_service.core.dto.centerSocialMedia.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCenterSocialMediaStatusRequest {
    
    @NotNull(message = "Is active status is required")
    private Boolean isActive;
}
