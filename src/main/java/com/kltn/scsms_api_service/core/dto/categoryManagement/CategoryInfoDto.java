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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryInfoDto extends AuditDto {
    
    @JsonProperty("category_id")
    private UUID categoryId;
    
    @JsonProperty("category_url")
    private String categoryUrl;
    
    @JsonProperty("category_name")
    private String categoryName;
    
    @JsonProperty("parent_category")
    private CategoryFlatDto parentCategory;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("type")
    private CategoryType type;
    
    @JsonProperty("subcategories")
    private List<CategoryFlatDto> subcategories;
    
    @JsonProperty("breadcrumb")
    private List<CategoryBreadcrumbDto> breadcrumb;
}
