package com.kltn.scsms_api_service.core.dto.serviceBayManagement.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.ServiceBay;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để cập nhật service bay
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceBayRequest {
    
    @NotBlank(message = "Bay name is required")
    @Size(max = 255, message = "Bay name must not exceed 255 characters")
    @JsonProperty("bay_name")
    private String bayName;
    
    @Size(max = 50, message = "Bay code must not exceed 50 characters")
    @JsonProperty("bay_code")
    private String bayCode;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("display_order")
    private Integer displayOrder;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @JsonProperty("notes")
    private String notes;
}
