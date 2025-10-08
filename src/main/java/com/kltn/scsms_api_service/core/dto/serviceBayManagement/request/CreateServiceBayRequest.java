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

import java.util.UUID;

/**
 * Request DTO để tạo service bay mới
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceBayRequest {
    
    @NotNull(message = "Branch ID is required")
    @JsonProperty("branch_id")
    private UUID branchId;
    
    @NotBlank(message = "Bay name is required")
    @Size(max = 255, message = "Bay name must not exceed 255 characters")
    @JsonProperty("bay_name")
    private String bayName;
    
    @Size(max = 50, message = "Bay code must not exceed 50 characters")
    @JsonProperty("bay_code")
    private String bayCode;
    
    @NotNull(message = "Bay type is required")
    @JsonProperty("bay_type")
    private ServiceBay.BayType bayType;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @JsonProperty("description")
    private String description;
    
    @Builder.Default
    @Positive(message = "Capacity must be positive")
    @JsonProperty("capacity")
    private Integer capacity = 1;
    
    @Builder.Default
    @JsonProperty("display_order")
    private Integer displayOrder = 1;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @JsonProperty("notes")
    private String notes;
}
