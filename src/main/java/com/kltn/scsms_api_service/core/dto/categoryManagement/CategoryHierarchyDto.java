package com.kltn.scsms_api_service.core.dto.categoryManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import com.kltn.scsms_api_service.core.entity.enumAttribute.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryHierarchyDto extends AuditDto {
    
    @JsonProperty("category_id")
    private UUID categoryId;
    
    @JsonProperty("category_url")
    private String categoryUrl;
    
    @JsonProperty("category_name")
    private String categoryName;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("type")
    private CategoryType type;
    
    @JsonProperty("level")
    private Integer level;
    
    @JsonProperty("has_children")
    private Boolean hasChildren;
    
    @JsonProperty("children")
    private List<CategoryHierarchyDto> children;
    
    @JsonProperty("parent_id")
    private UUID parentId;
    
    @JsonProperty("full_path")
    private String fullPath; // "Electronics > Smartphones > iPhone"
}
