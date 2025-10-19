package com.kltn.scsms_api_service.core.dto.serviceProcessTrackingManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Request DTO để bắt đầu thực hiện bước dịch vụ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartStepRequest {
    
    @JsonProperty("notes")
    private String notes;
}
