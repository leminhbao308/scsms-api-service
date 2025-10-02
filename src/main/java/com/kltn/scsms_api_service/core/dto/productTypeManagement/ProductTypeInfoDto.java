package com.kltn.scsms_api_service.core.dto.productTypeManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductTypeInfoDto {
    
    @JsonProperty("product_type_id")
    private UUID productTypeId;
    
    @JsonProperty("product_type_name")
    private String productTypeName;
    
    @JsonProperty("product_type_code")
    private String productTypeCode;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("category_id")
    private UUID categoryId;
    
    @JsonProperty("category_name")
    private String categoryName;
    
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
