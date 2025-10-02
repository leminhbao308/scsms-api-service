package com.kltn.scsms_api_service.core.dto.mediaManagement.request;

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
public class UpdateMediaMainStatusRequest {
    
    @NotNull(message = "Main status is required")
    @JsonProperty("is_main")
    private Boolean isMain;
}
