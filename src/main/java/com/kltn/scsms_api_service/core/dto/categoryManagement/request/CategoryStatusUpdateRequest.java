package com.kltn.scsms_api_service.core.dto.categoryManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryStatusUpdateRequest {
    
    @JsonProperty("is_active")
    @NotNull(message = "is_active is required")
    private Boolean isActive;
}
