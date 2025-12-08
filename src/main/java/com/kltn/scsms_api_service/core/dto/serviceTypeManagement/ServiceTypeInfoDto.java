package com.kltn.scsms_api_service.core.dto.serviceTypeManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for ServiceType information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTypeInfoDto {
    
    @JsonProperty("service_type_id")
    private UUID serviceTypeId;
    
    private String code;
    private String name;
    private String description;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("is_deleted")
    private Boolean isDeleted;
    
    @JsonProperty("display_name")
    private String displayName;
    
    @JsonProperty("created_date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdDate;
    
    @JsonProperty("modified_date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime modifiedDate;
    
    @JsonProperty("created_by")
    private String createdBy;
    
    @JsonProperty("modified_by")
    private String modifiedBy;
    
    private Long version;
}
