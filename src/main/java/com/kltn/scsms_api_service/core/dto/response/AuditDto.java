package com.kltn.scsms_api_service.core.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditDto {
    // Audit fields
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
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("is_deleted")
    private Boolean isDeleted;
}
