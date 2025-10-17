package com.kltn.scsms_api_service.core.dto.serviceBayManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO cho thông tin kỹ thuật viên trong service bay
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianInfoDto {
    
    @JsonProperty("technician_id")
    private UUID technicianId;
    
    @JsonProperty("technician_name")
    private String technicianName;
    
    @JsonProperty("technician_code")
    private String technicianCode;
    
    @JsonProperty("technician_phone")
    private String technicianPhone;
    
    @JsonProperty("technician_email")
    private String technicianEmail;
    
    @JsonProperty("is_active")
    private Boolean isActive;
}
