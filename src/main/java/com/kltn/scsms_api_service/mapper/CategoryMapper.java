package com.kltn.scsms_api_service.mapper;

import com.kltn.scsms_api_service.core.dto.categoryManagement.CategoryBreadcrumbDto;
import com.kltn.scsms_api_service.core.dto.categoryManagement.CategoryFlatDto;
import com.kltn.scsms_api_service.core.dto.categoryManagement.CategoryHierarchyDto;
import com.kltn.scsms_api_service.core.dto.categoryManagement.CategoryInfoDto;
import com.kltn.scsms_api_service.core.dto.categoryManagement.request.CategoryCreateRequest;
import com.kltn.scsms_api_service.core.dto.categoryManagement.request.CategoryUpdateRequest;
import com.kltn.scsms_api_service.core.entity.Category;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { AuditMapper.class })
public interface CategoryMapper {

    // ===== ENTITY TO DTO MAPPINGS =====

    /**
     * Map Category entity to CategoryFlatDto
     * Audit fields are inherited from AuditDto, so they're mapped automatically
     */
    @Mapping(target = "parentCategoryId", source = "parentCategory.categoryId")
    @Mapping(target = "subcategoryCount", expression = "java(getSubcategoryCount(category))")
    CategoryFlatDto toFlatDto(Category category);

    /**
     * Map list of Category entities to list of CategoryFlatDto
     */
    List<CategoryFlatDto> toFlatDtoList(List<Category> categories);

    /**
     * Map Category entity to CategoryInfoDto
     */
    @Mapping(target = "parentCategoryId", source = "parentCategory.categoryId")
    @Mapping(target = "subcategories", source = "subcategories", qualifiedByName = "toSubcategoryFlatDtoList")
    @Mapping(target = "breadcrumb", ignore = true)
    // Will be set manually in service
    CategoryInfoDto toInfoDto(Category category);

    /**
     * Map Category entity to CategoryBreadcrumbDto
     */
    @Mapping(target = "parentCategoryId", source = "parentCategory.categoryId")
    @Mapping(target = "level", ignore = true) // Will be set manually based on hierarchy
    @Mapping(target = "path", ignore = true) // Will be built manually
    @Mapping(target = "urlPath", ignore = true)
    // Will be built manually
    CategoryBreadcrumbDto toBreadcrumbDto(Category category);

    /**
     * Map Category entity to CategoryHierarchyDto
     */
    @Mapping(target = "parentId", source = "parentCategory.categoryId")
    @Mapping(target = "level", ignore = true) // Will be calculated
    @Mapping(target = "hasChildren", expression = "java(hasSubcategories(category))")
    @Mapping(target = "children", ignore = true) // Will be set manually
    @Mapping(target = "fullPath", ignore = true)
    // Will be built manually
    CategoryHierarchyDto toHierarchyDto(Category category);

    /**
     * Map list of Category entities to list of CategoryBreadcrumbDto
     */
    List<CategoryBreadcrumbDto> toBreadcrumbDtoList(List<Category> categories);

    // ===== DTO TO ENTITY MAPPINGS =====

    /**
     * Map CategoryCreateRequest to Category entity
     * Request DTOs don't extend AuditDto anymore, so map individual fields
     */
    Category toEntity(CategoryCreateRequest createRequest);

    /**
     * Update existing Category entity from CategoryUpdateRequest
     */
    @Mapping(target = "categoryId", ignore = true) // Don't update ID
    @Mapping(target = "subcategories", ignore = true) // Don't update subcategories here
    @Mapping(target = "parentCategory", ignore = true) // Will be handled by service
    @Mapping(target = "isActive", source = "isActive", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(CategoryUpdateRequest updateRequest, @MappingTarget Category category);

    // ===== BREADCRUMB & HIERARCHY HELPER METHODS =====

    /**
     * Build breadcrumb list from root to current category
     */
    default List<CategoryBreadcrumbDto> buildBreadcrumb(Category category) {
        List<CategoryBreadcrumbDto> breadcrumb = new ArrayList<>();
        if (category == null) {
            return breadcrumb;
        }

        List<Category> ancestors = getAncestors(category);

        for (int i = 0; i < ancestors.size(); i++) {
            Category ancestor = ancestors.get(i);
            CategoryBreadcrumbDto dto = toBreadcrumbDto(ancestor);
            dto.setLevel(i);

            // Build path and urlPath
            String path = ancestors.subList(0, i + 1)
                    .stream()
                    .map(Category::getCategoryName)
                    .collect(Collectors.joining(" > "));
            dto.setPath(path);

            String urlPath = "/" + ancestors.subList(0, i + 1)
                    .stream()
                    .map(Category::getCategoryUrl)
                    .collect(Collectors.joining("/"));
            dto.setUrlPath(urlPath);

            breadcrumb.add(dto);
        }

        return breadcrumb;
    }

    /**
     * Get all ancestors from root to current category
     */
    default List<Category> getAncestors(Category category) {
        List<Category> ancestors = new ArrayList<>();
        Category current = category;

        // Build path from current to root
        while (current != null) {
            ancestors.addFirst(current); // Add at beginning to maintain order
            current = current.getParentCategory();
        }

        return ancestors;
    }

    /**
     * Build full hierarchy tree from list of categories
     */
    default List<CategoryHierarchyDto> buildHierarchyTree(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return new ArrayList<>();
        }

        // Find root categories (no parent)
        List<Category> rootCategories = categories.stream()
                .filter(c -> c.getParentCategory() == null)
                .toList();

        return rootCategories.stream()
                .map(root -> buildHierarchyNode(root, categories, 0))
                .collect(Collectors.toList());
    }

    /**
     * Build hierarchy node with children
     */
    default CategoryHierarchyDto buildHierarchyNode(Category category, List<Category> allCategories, int level) {
        CategoryHierarchyDto dto = toHierarchyDto(category);
        dto.setLevel(level);

        // Build full path
        List<Category> ancestors = getAncestors(category);
        String fullPath = ancestors.stream()
                .map(Category::getCategoryName)
                .collect(Collectors.joining(" > "));
        dto.setFullPath(fullPath);

        // Find children
        List<Category> children = allCategories.stream()
                .filter(c -> c.getParentCategory() != null &&
                        c.getParentCategory().getCategoryId().equals(category.getCategoryId()))
                .toList();

        // Recursively build children
        List<CategoryHierarchyDto> childrenDtos = children.stream()
                .map(child -> buildHierarchyNode(child, allCategories, level + 1))
                .collect(Collectors.toList());

        dto.setChildren(childrenDtos);
        dto.setHasChildren(!childrenDtos.isEmpty());

        return dto;
    }

    // ===== HELPER METHODS =====

    /**
     * Custom method to calculate subcategory count
     * Note: This method should be used with caution as it may trigger LAZY loading
     * For better performance, use repository queries to get subcategory count
     */
    default Integer getSubcategoryCount(Category category) {
        if (category == null) {
            return 0;
        }
        // Check if subcategories are already loaded to avoid LAZY loading issues
        try {
            if (category.getSubcategories() != null) {
                return category.getSubcategories().size();
            }
        } catch (Exception e) {
            // If LAZY loading fails, return 0 and log warning
            // The actual count should be fetched from repository
            return 0;
        }
        return 0;
    }

    // ===== SPECIALIZED MAPPINGS =====

    /**
     * Map Category to CategoryFlatDto for parent category (without subcategory
     * count)
     */
    @Named("toParentFlatDto")
    @Mapping(target = "parentCategoryId", source = "parentCategory.categoryId")
    @Mapping(target = "subcategoryCount", ignore = true)
    // Not needed for parent reference
    CategoryFlatDto toParentFlatDto(Category category);

    /**
     * Map Category to CategoryFlatDto for subcategory
     */
    @Named("toSubcategoryFlatDto")
    @Mapping(target = "parentCategoryId", source = "parentCategory.categoryId")
    @Mapping(target = "subcategoryCount", expression = "java(getSubcategoryCount(category))")
    CategoryFlatDto toSubcategoryFlatDto(Category category);

    /**
     * Map list of Category entities to list of CategoryFlatDto for subcategories
     */
    @Named("toSubcategoryFlatDtoList")
    @IterableMapping(qualifiedByName = "toSubcategoryFlatDto")
    List<CategoryFlatDto> toSubcategoryFlatDtoList(List<Category> categories);

    /**
     * Map Category to CategoryInfoDto with conditional parent mapping
     */
    @Mapping(target = "parentCategoryId", source = "parentCategory.categoryId")
    @Mapping(target = "subcategories", source = "subcategories", qualifiedByName = "toSubcategoryFlatDtoList")
    @Mapping(target = "breadcrumb", expression = "java(buildBreadcrumb(category))")
    CategoryInfoDto toDetailedInfoDto(Category category);

    // ===== BULK OPERATIONS =====

    /**
     * Convert for pagination responses
     */
    @Named("toPaginationFlatDto")
    @Mapping(target = "parentCategoryId", source = "parentCategory.categoryId")
    @Mapping(target = "subcategoryCount", expression = "java(getSubcategoryCount(category))")
    CategoryFlatDto toPaginationFlatDto(Category category);

    /**
     * Convert list of categories for pagination
     */
    @IterableMapping(qualifiedByName = "toPaginationFlatDto")
    List<CategoryFlatDto> toPaginationFlatDtoList(List<Category> categories);

    // ===== UTILITY MAPPINGS =====

    /**
     * Create breadcrumb item with level and paths
     */
    default CategoryBreadcrumbDto toBreadcrumbDtoWithLevel(Category category, int level, String path, String urlPath) {
        if (category == null) {
            return null;
        }
        CategoryBreadcrumbDto dto = toBreadcrumbDto(category);
        dto.setLevel(level);
        dto.setPath(path);
        dto.setUrlPath(urlPath);
        return dto;
    }

    /**
     * Map minimal category info (for dropdowns, selects)
     */
    @Named("toMinimalDto")
    @Mapping(target = "parentCategoryId", source = "parentCategory.categoryId")
    @Mapping(target = "level", ignore = true)
    @Mapping(target = "path", ignore = true)
    @Mapping(target = "urlPath", ignore = true)
    CategoryBreadcrumbDto toMinimalDto(Category category);

    /**
     * Convert list for dropdown/select options
     */
    @IterableMapping(qualifiedByName = "toMinimalDto")
    List<CategoryBreadcrumbDto> toMinimalDtoList(List<Category> categories);

    // ===== VALIDATION HELPERS =====

    /**
     * Check if category has parent (for validation purposes)
     */
    default boolean hasParent(Category category) {
        return category != null && category.getParentCategory() != null;
    }

    /**
     * Check if category has subcategories (for deletion validation)
     * Note: This method should be used with caution as it may trigger LAZY loading
     * For better performance, use repository queries to check subcategory existence
     */
    default boolean hasSubcategories(Category category) {
        if (category == null) {
            return false;
        }
        // Check if subcategories are already loaded to avoid LAZY loading issues
        try {
            return category.getSubcategories() != null && !category.getSubcategories().isEmpty();
        } catch (Exception e) {
            // If LAZY loading fails, return false and log warning
            // The actual check should be done via repository
            return false;
        }
    }
}
