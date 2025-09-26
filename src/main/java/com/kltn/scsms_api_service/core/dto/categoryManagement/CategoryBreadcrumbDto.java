package com.kltn.scsms_api_service.core.dto.categoryManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryBreadcrumbDto extends AuditDto {
    
    @JsonProperty("category_id")
    private UUID categoryId;
    
    @JsonProperty("category_url")
    private String categoryUrl;
    
    @JsonProperty("category_name")
    private String categoryName;
    
    @JsonProperty("parent_category_id")
    private UUID parentCategoryId;
    
    @JsonProperty("level")
    private Integer level; // Cấp độ trong hierarchy (0 = root, 1 = child, ...)
    
    @JsonProperty("path")
    private String path; // Full path như "Electronics > Smartphones > iPhone"
    
    @JsonProperty("url_path")
    private String urlPath; // URL path như "/electronics/smartphones/iphone"
}
