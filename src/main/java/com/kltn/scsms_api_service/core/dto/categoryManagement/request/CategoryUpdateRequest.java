package com.kltn.scsms_api_service.core.dto.categoryManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import com.kltn.scsms_api_service.core.entity.enumAttribute.CategoryType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryUpdateRequest {
    
    @JsonProperty("category_url")
    private String categoryUrl;
    
    @JsonProperty("category_name")
    private String categoryName;
    
    @JsonProperty("parent_category_id")
    private UUID parentCategoryId;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("type")
    private CategoryType type;
}
