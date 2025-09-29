package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.categoryManagement.CategoryBreadcrumbDto;
import com.kltn.scsms_api_service.core.dto.categoryManagement.CategoryHierarchyDto;
import com.kltn.scsms_api_service.core.dto.categoryManagement.CategoryInfoDto;
import com.kltn.scsms_api_service.core.dto.categoryManagement.param.CategoryFilterParam;
import com.kltn.scsms_api_service.core.dto.categoryManagement.request.CategoryCreateRequest;
import com.kltn.scsms_api_service.core.dto.categoryManagement.request.CategoryStatusUpdateRequest;
import com.kltn.scsms_api_service.core.dto.categoryManagement.request.CategoryUpdateRequest;
import com.kltn.scsms_api_service.core.service.entityService.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryManagementService {
    
    private final CategoryService categoryService;
    
    /**
     * Get all categories with pagination and filtering
     */
    public Page<CategoryInfoDto> getAllCategories(CategoryFilterParam filterParam) {
        log.debug("Fetching categories with filter: {}", filterParam);
        return categoryService.getAllCategories(filterParam);
    }
    
    /**
     * Get categories in hierarchical structure
     */
    public List<CategoryHierarchyDto> getCategoryHierarchy(String type, UUID parentId, int maxDepth) {
        log.debug("Fetching category hierarchy - type: {}, parentId: {}, maxDepth: {}",
            type, parentId, maxDepth);
        return categoryService.getCategoryHierarchy(type, parentId, maxDepth);
    }
    
    /**
     * Get all categories with breadcrumb information
     */
    public List<CategoryBreadcrumbDto> getAllCategoriesWithBreadcrumb(String type, UUID parentId) {
        log.debug("Fetching categories with breadcrumb - type: {}, parentId: {}", type, parentId);
        return categoryService.getAllCategoriesWithBreadcrumb(type, parentId);
    }
    
    /**
     * Get category by ID with detailed information
     */
    public CategoryInfoDto getCategoryById(UUID categoryId) {
        log.debug("Fetching category by ID: {}", categoryId);
        return categoryService.getCategoryById(categoryId);
    }
    
    /**
     * Get category by URL slug
     */
    public CategoryInfoDto getCategoryByUrl(String categoryUrl) {
        log.debug("Fetching category by URL: {}", categoryUrl);
        return categoryService.getCategoryByUrl(categoryUrl);
    }
    
    /**
     * Get subcategories of a specific category
     */
    public List<CategoryInfoDto> getSubcategories(UUID categoryId, boolean includeInactive) {
        log.debug("Fetching subcategories for category: {}, includeInactive: {}",
            categoryId, includeInactive);
        return categoryService.getSubcategories(categoryId, includeInactive);
    }
    
    /**
     * Create a new category
     */
    @Transactional
    public CategoryInfoDto createCategory(CategoryCreateRequest createRequest) {
        log.info("Creating category: {}", createRequest.getCategoryName());
        
        // Validate request
        validateCategoryCreateRequest(createRequest);
        
        // Check URL uniqueness
        if (!validateCategoryUrl(createRequest.getCategoryUrl(), null)) {
            throw new IllegalArgumentException("Category URL already exists: " + createRequest.getCategoryUrl());
        }
        
        return categoryService.createCategory(createRequest);
    }
    
    /**
     * Update an existing category
     */
    @Transactional
    public CategoryInfoDto updateCategory(UUID categoryId, CategoryUpdateRequest updateRequest) {
        log.info("Updating category: {}", categoryId);
        
        // Validate request
        validateCategoryUpdateRequest(categoryId, updateRequest);
        
        // Check URL uniqueness if URL is being changed
        if (updateRequest.getCategoryUrl() != null
            && !validateCategoryUrl(updateRequest.getCategoryUrl(), categoryId)) {
            throw new IllegalArgumentException("Category URL already exists: " + updateRequest.getCategoryUrl());
        }
        
        return categoryService.updateCategory(categoryId, updateRequest);
    }
    
    /**
     * Delete a category (soft delete)
     */
    @Transactional
    public void deleteCategory(UUID categoryId, boolean force) {
        log.info("Deleting category: {}, force: {}", categoryId, force);
        
        // Validate deletion
        if (!force && hasSubcategories(categoryId)) {
            throw new IllegalArgumentException("Cannot delete category with subcategories. Use force=true to delete anyway.");
        }
        
        categoryService.deleteCategory(categoryId, force);
    }
    
    /**
     * Move category to different parent
     */
    @Transactional
    public CategoryInfoDto moveCategory(UUID categoryId, UUID newParentId) {
        log.info("Moving category {} to parent: {}", categoryId, newParentId);
        
        // Validate move operation
        validateCategoryMove(categoryId, newParentId);
        
        return categoryService.moveCategory(categoryId, newParentId);
    }
    
    /**
     * Get category path from root to specified category
     */
    public List<CategoryBreadcrumbDto> getCategoryPath(UUID categoryId) {
        log.debug("Fetching category path for: {}", categoryId);
        return categoryService.getCategoryPath(categoryId);
    }
    
    /**
     * Validate category URL uniqueness
     */
    public boolean validateCategoryUrl(String categoryUrl, UUID excludeCategoryId) {
        log.debug("Validating category URL: {}, excluding: {}", categoryUrl, excludeCategoryId);
        return categoryService.isCategoryUrlUnique(categoryUrl, excludeCategoryId);
    }
    
    /**
     * Get root categories (categories without parent)
     */
    public List<CategoryInfoDto> getRootCategories(String type) {
        log.debug("Fetching root categories with type: {}", type);
        return categoryService.getRootCategories(type);
    }
    
    // Private validation methods
    
    private void validateCategoryCreateRequest(CategoryCreateRequest request) {
        if (request.getCategoryName() == null || request.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }
        
        if (request.getCategoryUrl() == null || request.getCategoryUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Category URL is required");
        }
        
        // Validate parent category exists
        if (request.getParentCategoryId() != null) {
            if (!categoryService.existsById(request.getParentCategoryId())) {
                throw new IllegalArgumentException("Parent category not found: " + request.getParentCategoryId());
            }
        }
        
        // Validate URL format
        validateUrlFormat(request.getCategoryUrl());
    }
    
    private void validateCategoryUpdateRequest(UUID categoryId, CategoryUpdateRequest request) {
        // Check if category exists
        if (!categoryService.existsById(categoryId)) {
            throw new IllegalArgumentException("Category not found: " + categoryId);
        }
        
        // Validate parent category exists if specified
        if (request.getParentCategoryId() != null) {
            if (!categoryService.existsById(request.getParentCategoryId())) {
                throw new IllegalArgumentException("Parent category not found: " + request.getParentCategoryId());
            }
            
            // Prevent setting self as parent
            if (request.getParentCategoryId().equals(categoryId)) {
                throw new IllegalArgumentException("Category cannot be its own parent");
            }
            
            // Prevent circular references
            if (wouldCreateCircularReference(categoryId, request.getParentCategoryId())) {
                throw new IllegalArgumentException("Update would create circular reference in category hierarchy");
            }
        }
        
        // Validate URL format if provided
        if (request.getCategoryUrl() != null) {
            validateUrlFormat(request.getCategoryUrl());
        }
    }
    
    private void validateCategoryMove(UUID categoryId, UUID newParentId) {
        // Check if category exists
        if (!categoryService.existsById(categoryId)) {
            throw new IllegalArgumentException("Category not found: " + categoryId);
        }
        
        // Check if new parent exists (null means move to root)
        if (newParentId != null && !categoryService.existsById(newParentId)) {
            throw new IllegalArgumentException("New parent category not found: " + newParentId);
        }
        
        // Prevent setting self as parent
        if (newParentId != null && newParentId.equals(categoryId)) {
            throw new IllegalArgumentException("Category cannot be its own parent");
        }
        
        // Prevent circular references
        if (newParentId != null && wouldCreateCircularReference(categoryId, newParentId)) {
            throw new IllegalArgumentException("Move would create circular reference in category hierarchy");
        }
    }
    
    private void validateUrlFormat(String categoryUrl) {
        if (categoryUrl == null || categoryUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Category URL cannot be empty");
        }
        
        String url = categoryUrl.trim().toLowerCase();
        
        // Check for invalid characters
        if (!url.matches("^[a-z0-9\\-_/]+$")) {
            throw new IllegalArgumentException("Category URL can only contain lowercase letters, numbers, hyphens, underscores, and forward slashes");
        }
        
        // Check for consecutive slashes
        if (url.contains("//")) {
            throw new IllegalArgumentException("Category URL cannot contain consecutive slashes");
        }
        
        // Check length
        if (url.length() > 255) {
            throw new IllegalArgumentException("Category URL cannot exceed 255 characters");
        }
    }
    
    private boolean wouldCreateCircularReference(UUID categoryId, UUID potentialParentId) {
        return categoryService.wouldCreateCircularReference(categoryId, potentialParentId);
    }
    
    private boolean hasSubcategories(UUID categoryId) {
        return categoryService.hasSubcategories(categoryId);
    }
    
    /**
     * Get category statistics for dashboard
     */
    public CategoryStatsDto getCategoryStatistics() {
        log.debug("Fetching category statistics");
        return categoryService.getCategoryStatistics();
    }
    
    /**
     * Bulk create categories from import
     */
    @Transactional
    public List<CategoryInfoDto> bulkCreateCategories(List<CategoryCreateRequest> createRequests) {
        log.info("Bulk creating {} categories", createRequests.size());
        
        // Validate all requests first
        for (CategoryCreateRequest request : createRequests) {
            validateCategoryCreateRequest(request);
        }
        
        return categoryService.bulkCreateCategories(createRequests);
    }
    
    /**
     * Reorder categories within the same parent
     */
    @Transactional
    public List<CategoryInfoDto> reorderCategories(UUID parentId, List<UUID> categoryIds) {
        log.info("Reordering {} categories under parent: {}", categoryIds.size(), parentId);
        
        // Validate all categories belong to the same parent
        for (UUID categoryId : categoryIds) {
            if (!categoryService.belongsToParent(categoryId, parentId)) {
                throw new IllegalArgumentException("Category " + categoryId + " does not belong to parent " + parentId);
            }
        }
        
        return categoryService.reorderCategories(parentId, categoryIds);
    }
    
    /**
     * Search categories by name with fuzzy matching
     */
    public List<CategoryInfoDto> searchCategories(String searchTerm, int limit) {
        log.debug("Searching categories with term: {}, limit: {}", searchTerm, limit);
        return categoryService.searchCategories(searchTerm, limit);
    }
    
    /**
     * Get category suggestions for autocomplete
     */
    public List<CategoryInfoDto> getCategorySuggestions(String partial, UUID parentId, int limit) {
        log.debug("Getting category suggestions for: {}, parent: {}, limit: {}", partial, parentId, limit);
        return categoryService.getCategorySuggestions(partial, parentId, limit);
    }
    
    /**
     * Update category status (isActive) only
     */
    @Transactional
    public CategoryInfoDto updateCategoryStatus(UUID categoryId, CategoryStatusUpdateRequest statusRequest) {
        log.info("Updating category status: {} to {}", categoryId, statusRequest.getIsActive());
        
        // Validate category exists
        if (!categoryService.existsById(categoryId)) {
            throw new IllegalArgumentException("Category not found: " + categoryId);
        }
        
        return categoryService.updateCategoryStatus(categoryId, statusRequest.getIsActive());
    }
    
    
    // Inner class for statistics DTO
    public static class CategoryStatsDto {
        private long totalCategories;
        private long rootCategories;
        private long leafCategories;
        private int maxDepth;
        private long categoriesWithSubcategories;
        
        // Constructors, getters, and setters
        public CategoryStatsDto() {}
        
        public CategoryStatsDto(long totalCategories, long rootCategories, long leafCategories,
                                int maxDepth, long categoriesWithSubcategories) {
            this.totalCategories = totalCategories;
            this.rootCategories = rootCategories;
            this.leafCategories = leafCategories;
            this.maxDepth = maxDepth;
            this.categoriesWithSubcategories = categoriesWithSubcategories;
        }
        
        // Getters and setters
        public long getTotalCategories() { return totalCategories; }
        public void setTotalCategories(long totalCategories) { this.totalCategories = totalCategories; }
        
        public long getRootCategories() { return rootCategories; }
        public void setRootCategories(long rootCategories) { this.rootCategories = rootCategories; }
        
        public long getLeafCategories() { return leafCategories; }
        public void setLeafCategories(long leafCategories) { this.leafCategories = leafCategories; }
        
        public int getMaxDepth() { return maxDepth; }
        public void setMaxDepth(int maxDepth) { this.maxDepth = maxDepth; }
        
        public long getCategoriesWithSubcategories() { return categoriesWithSubcategories; }
        public void setCategoriesWithSubcategories(long categoriesWithSubcategories) {
            this.categoriesWithSubcategories = categoriesWithSubcategories;
        }
    }
}
