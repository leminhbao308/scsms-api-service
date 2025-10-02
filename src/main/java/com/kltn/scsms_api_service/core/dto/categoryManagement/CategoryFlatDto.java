package com.kltn.scsms_api_service.core.dto.categoryManagement;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.response.AuditDto;
import com.kltn.scsms_api_service.core.entity.enumAttribute.CategoryType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryFlatDto extends AuditDto {
    
    @JsonProperty("category_id")
    private UUID categoryId;
    
    @JsonProperty("category_code")
    private String categoryCode;
    
    @JsonProperty("category_name")
    private String categoryName;
    
    @JsonProperty("category_url")
    private String categoryUrl;
    
    @JsonProperty("parent_category_id")
    private UUID parentCategoryId;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("category_type")
    private CategoryType categoryType;
    
    @JsonProperty("level")
    private Integer level;
    
    @JsonProperty("sort_order")
    private Integer sortOrder;
    
    @JsonProperty("subcategory_count")
    private Integer subcategoryCount;
}
