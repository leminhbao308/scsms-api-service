package com.kltn.scsms_api_service.core.dto.productAttributeManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeInfoDto {
    
    @JsonProperty("attribute_id")
    private UUID attributeId;
    
    @JsonProperty("attribute_name")
    private String attributeName;
    
    @JsonProperty("attribute_code")
    private String attributeCode;
    
    @JsonProperty("unit")
    private String unit;
    
    @JsonProperty("is_required")
    private Boolean isRequired;
    
    @JsonProperty("data_type")
    private String dataType;
    
    @JsonProperty("display_name")
    private String displayName;
    
    // Audit fields
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("created_date")
    private LocalDateTime createdDate;
    
    @JsonProperty("modified_date")
    private LocalDateTime modifiedDate;
    
    @JsonProperty("created_by")
    private String createdBy;
    
    @JsonProperty("modified_by")
    private String modifiedBy;
}
