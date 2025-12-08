package com.kltn.scsms_api_service.core.dto.categoryManagement.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.entity.enumAttribute.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryCreateRequest{
    
    @Size(max = 50, message = "Category code must not exceed 50 characters")
    @JsonProperty("category_code")
    private String categoryCode;
    
    @NotBlank(message = "Category name is required")
    @Size(max = 255, message = "Category name must not exceed 255 characters")
    @JsonProperty("category_name")
    private String categoryName;
    
    @NotBlank(message = "Category URL is required")
    @Size(max = 255, message = "Category URL must not exceed 255 characters")
    @JsonProperty("category_url")
    private String categoryUrl;
    
    @JsonProperty("parent_category_id")
    private UUID parentCategoryId;
    
    @JsonProperty("description")
    private String description;
    
    @NotNull(message = "Category type is required")
    @JsonProperty("category_type")
    private CategoryType categoryType;
    
    @JsonProperty("level")
    private Integer level;
    
    @JsonProperty("sort_order")
    private Integer sortOrder;
}
