package com.kltn.scsms_api_service.core.dto.categoryManagement.param;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.abstracts.BaseFilterParam;
import com.kltn.scsms_api_service.core.entity.enumAttribute.CategoryType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryFilterParam extends BaseFilterParam<CategoryFilterParam> {
    
    // Category-specific filters
    private CategoryType type;
    
    @JsonProperty("parent_category_id")
    private UUID parentCategoryId;
    
    @JsonProperty("has_parent")
    private Boolean hasParent;
    
    @JsonProperty("has_subcategories")
    private Boolean hasSubcategories;
    
    @JsonProperty("level")
    private Integer level; // Hierarchy level (0 = root, 1 = first level child, etc.)
    
    @JsonProperty("max_level")
    private Integer maxLevel; // Maximum hierarchy level to include
    
    @JsonProperty("include_subcategories")
    private Boolean includeSubcategories; // Include subcategories in response
    
    // Category-specific search fields
    @JsonProperty("category_name")
    @Size(min = 1, message = "Category name must be at least 1 character")
    private String categoryName;
    
    @JsonProperty("category_url")
    private String categoryUrl;
    
    private String description;
    
    @JsonProperty("full_path")
    private String fullPath; // Search in full category path
    
    // Filter by specific parent category name
    @JsonProperty("parent_category_name")
    private String parentCategoryName;
    
    // Filter by root category (categories at the top level)
    @JsonProperty("root_only")
    private Boolean rootOnly;
    
    // Filter by leaf categories (categories without children)
    @JsonProperty("leaf_only")
    private Boolean leafOnly;
    
    public static CategoryFilterParam standardize(CategoryFilterParam categoryFilterParam) {
        return categoryFilterParam.standardizeFilterRequest(categoryFilterParam);
    }
    
    @Override
    protected String getDefaultSortField() {
        return "categoryName"; // Category specific default sort field
    }
    
    @Override
    protected void standardizeSpecificFields(CategoryFilterParam request) {
        // Standardize search terms
        request.setCategoryName(trimAndNullify(request.getCategoryName()));
        request.setCategoryUrl(trimAndNullify(request.getCategoryUrl()));
        request.setDescription(trimAndNullify(request.getDescription()));
        request.setFullPath(trimAndNullify(request.getFullPath()));
        request.setParentCategoryName(trimAndNullify(request.getParentCategoryName()));
        
        // Standardize URL - remove leading/trailing slashes and convert to lowercase
        if (request.getCategoryUrl() != null) {
            String url = request.getCategoryUrl().trim().toLowerCase();
            // Remove leading and trailing slashes
            url = url.replaceAll("^/+|/+$", "");
            request.setCategoryUrl(url.isEmpty() ? null : url);
        }
        
        // Validate level constraints
        if (request.getLevel() != null && request.getLevel() < 0) {
            request.setLevel(0);
        }
        if (request.getMaxLevel() != null && request.getMaxLevel() < 0) {
            request.setMaxLevel(null);
        }
        
        // Ensure level <= maxLevel if both are specified
        if (request.getLevel() != null && request.getMaxLevel() != null
            && request.getLevel() > request.getMaxLevel()) {
            request.setMaxLevel(request.getLevel());
        }
        
        // Handle contradictory filters
        if (Boolean.TRUE.equals(request.getRootOnly()) && request.getParentCategoryId() != null) {
            // If rootOnly is true, clear parentCategoryId as they contradict each other
            request.setParentCategoryId(null);
        }
        
        if (Boolean.TRUE.equals(request.getRootOnly())) {
            request.setHasParent(false);
            request.setLevel(0);
        }
        
        if (Boolean.TRUE.equals(request.getLeafOnly())) {
            request.setHasSubcategories(false);
        }
    }
    
    /**
     * Check if this filter is for root categories only
     */
    public boolean isRootCategoriesOnly() {
        return Boolean.TRUE.equals(rootOnly) ||
            (Boolean.FALSE.equals(hasParent) && parentCategoryId == null) ||
            (level != null && level == 0);
    }
    
    /**
     * Check if this filter is for leaf categories only
     */
    public boolean isLeafCategoriesOnly() {
        return Boolean.TRUE.equals(leafOnly) || Boolean.FALSE.equals(hasSubcategories);
    }
    
    /**
     * Get the effective maximum level for hierarchy queries
     */
    public Integer getEffectiveMaxLevel() {
        if (maxLevel != null) {
            return maxLevel;
        }
        if (Boolean.TRUE.equals(rootOnly)) {
            return 0;
        }
        return null; // No limit
    }
}
