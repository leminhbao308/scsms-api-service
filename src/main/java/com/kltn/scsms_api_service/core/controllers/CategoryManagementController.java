package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.RequireRole;
import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.categoryManagement.CategoryInfoDto;
import com.kltn.scsms_api_service.core.dto.categoryManagement.CategoryHierarchyDto;
import com.kltn.scsms_api_service.core.dto.categoryManagement.CategoryBreadcrumbDto;
import com.kltn.scsms_api_service.core.dto.categoryManagement.param.CategoryFilterParam;
import com.kltn.scsms_api_service.core.dto.categoryManagement.request.CategoryCreateRequest;
import com.kltn.scsms_api_service.core.dto.categoryManagement.request.CategoryUpdateRequest;
import com.kltn.scsms_api_service.core.dto.categoryManagement.request.CategoryStatusUpdateRequest;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.response.PaginatedResponse;
import com.kltn.scsms_api_service.core.service.businessService.CategoryManagementService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Controller handling category management operations
 * Manages hierarchical category structure, CRUD operations, and breadcrumb navigation
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Category Management", description = "Category management endpoints for hierarchical category operations")
public class CategoryManagementController {
    
    private final CategoryManagementService categoryManagementService;
    
    /**
     * Get all categories with pagination and filtering
     */
    @GetMapping(ApiConstant.GET_ALL_CATEGORIES_API)
    @SwaggerOperation(
        summary = "Get all categories",
        description = "Retrieve a paginated list of all categories that can be filtered by type, parent category, etc.")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<PaginatedResponse<CategoryInfoDto>>> getAllCategories(
        @ModelAttribute CategoryFilterParam categoryFilterParam) {
        log.info("Fetching all categories with filters: {}", categoryFilterParam);
        
        Page<CategoryInfoDto> categories = categoryManagementService.getAllCategories(
            CategoryFilterParam.standardize(categoryFilterParam));
        
        return ResponseBuilder.paginated("Categories fetched successfully", categories);
    }
    
    /**
     * Get categories in hierarchical structure for tree views
     */
    @GetMapping(ApiConstant.GET_ALL_CATEGORIES_HIERARCHY_API)
    @SwaggerOperation(
        summary = "Get category hierarchy",
        description = "Retrieve categories in hierarchical tree structure for navigation menus and tree views")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<List<CategoryHierarchyDto>>> getCategoryHierarchy(
        @RequestParam(required = false) String type,
        @RequestParam(required = false) UUID parentId,
        @RequestParam(defaultValue = "3") int maxDepth) {
        log.info("Fetching category hierarchy with type: {}, parentId: {}, maxDepth: {}",
            type, parentId, maxDepth);
        
        List<CategoryHierarchyDto> hierarchy = categoryManagementService.getCategoryHierarchy(type, parentId, maxDepth);
        
        return ResponseBuilder.success("Category hierarchy fetched successfully", hierarchy);
    }
    
    /**
     * Get all categories with breadcrumb information
     */
    @GetMapping(ApiConstant.GET_ALL_CATEGORIES_BREADCRUMB_API)
    @SwaggerOperation(
        summary = "Get categories with breadcrumb",
        description = "Retrieve categories with breadcrumb navigation path for UI components")
    public ResponseEntity<ApiResponse<List<CategoryBreadcrumbDto>>> getAllCategoriesWithBreadcrumb(
        @RequestParam(required = false) String type,
        @RequestParam(required = false) UUID parentId) {
        log.info("Fetching categories with breadcrumb - type: {}, parentId: {}", type, parentId);
        
        List<CategoryBreadcrumbDto> categoriesWithBreadcrumb =
            categoryManagementService.getAllCategoriesWithBreadcrumb(type, parentId);
        
        return ResponseBuilder.success("Categories with breadcrumb fetched successfully", categoriesWithBreadcrumb);
    }
    
    /**
     * Get category by ID with detailed information
     */
    @GetMapping(ApiConstant.GET_CATEGORY_BY_ID_API)
    @SwaggerOperation(
        summary = "Get category by ID",
        description = "Retrieve detailed information about a specific category including parent and subcategories")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<CategoryInfoDto>> getCategoryById(
        @PathVariable("categoryId") String categoryId) {
        log.info("Fetching category with ID: {}", categoryId);
        
        CategoryInfoDto category = categoryManagementService.getCategoryById(UUID.fromString(categoryId));
        
        return ResponseBuilder.success("Category fetched successfully", category);
    }
    
    /**
     * Get category by URL slug
     */
    @GetMapping(ApiConstant.GET_CATEGORY_URL_BY_ID_API)
    @SwaggerOperation(
        summary = "Get category by URL",
        description = "Retrieve category information using URL slug for SEO-friendly routing")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<CategoryInfoDto>> getCategoryByUrl(
        @RequestParam String categoryUrl) {
        log.info("Fetching category with URL: {}", categoryUrl);
        
        CategoryInfoDto category = categoryManagementService.getCategoryByUrl(categoryUrl);
        
        return ResponseBuilder.success("Category fetched successfully", category);
    }
    
    /**
     * Create a new category
     */
    @PostMapping(ApiConstant.CREATE_CATEGORY_API)
    @SwaggerOperation(
        summary = "Create a new category",
        description = "Create a new category with hierarchical parent-child relationships")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<CategoryInfoDto>> createCategory(
        @Valid @RequestBody CategoryCreateRequest createRequest) {
        log.info("Creating new category with name: {} and URL: {}",
            createRequest.getCategoryName(), createRequest.getCategoryUrl());
        
        CategoryInfoDto createdCategory = categoryManagementService.createCategory(createRequest);
        
        return ResponseBuilder.success("Category created successfully", createdCategory);
    }
    
    /**
     * Get subcategories of a specific category
     */
    @GetMapping(ApiConstant.GET_SUB_CATEGORY_BY_ID_API)
    @SwaggerOperation(
        summary = "Get subcategories",
        description = "Retrieve all direct subcategories of a specific parent category")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<List<CategoryInfoDto>>> getSubcategories(
        @PathVariable("categoryId") String categoryId,
        @RequestParam(defaultValue = "false") boolean includeInactive) {
        log.info("Fetching subcategories for category ID: {}, includeInactive: {}",
            categoryId, includeInactive);
        
        List<CategoryInfoDto> subcategories = categoryManagementService.getSubcategories(
            UUID.fromString(categoryId), includeInactive);
        
        return ResponseBuilder.success("Subcategories fetched successfully", subcategories);
    }
    
    /**
     * Update an existing category
     */
    @PostMapping(ApiConstant.UPDATE_CATEGORY_API)
    @SwaggerOperation(
        summary = "Update an existing category",
        description = "Update category details including name, URL, parent relationship, type, and active status")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<CategoryInfoDto>> updateCategory(
        @PathVariable("categoryId") String categoryId,
        @Valid @RequestBody CategoryUpdateRequest updateRequest) {
        log.info("Updating category with ID: {}", categoryId);
        
        CategoryInfoDto updatedCategory = categoryManagementService.updateCategory(
            UUID.fromString(categoryId), updateRequest);
        
        return ResponseBuilder.success("Category updated successfully", updatedCategory);
    }
    
    /**
     * Delete a category (soft delete)
     */
    @PostMapping(ApiConstant.DELETE_CATEGORY_API)
    @SwaggerOperation(
        summary = "Delete a category",
        description = "Soft delete a category. Categories with subcategories cannot be deleted unless force flag is used")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
        @PathVariable("categoryId") String categoryId,
        @RequestParam(defaultValue = "false") boolean force) {
        log.info("Deleting category with ID: {}, force: {}", categoryId, force);
        
        categoryManagementService.deleteCategory(UUID.fromString(categoryId), force);
        
        return ResponseBuilder.success("Category deleted successfully");
    }
    
    /**
     * Move category to different parent (change hierarchy)
     */
    @PostMapping(ApiConstant.MOVE_CATEGORY_API)
    @SwaggerOperation(
        summary = "Move category to different parent",
        description = "Change the parent-child relationship of a category within the hierarchy")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<CategoryInfoDto>> moveCategory(
        @PathVariable("categoryId") String categoryId,
        @RequestParam(required = false) UUID newParentId) {
        log.info("Moving category {} to new parent: {}", categoryId, newParentId);
        
        CategoryInfoDto movedCategory = categoryManagementService.moveCategory(
            UUID.fromString(categoryId), newParentId);
        
        return ResponseBuilder.success("Category moved successfully", movedCategory);
    }
    
    /**
     * Get category path from root to specified category
     */
    @GetMapping(ApiConstant.GET_CATEGORY_PATH_BY_ID_API)
    @SwaggerOperation(
        summary = "Get category path",
        description = "Get the complete path from root category to the specified category")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<List<CategoryBreadcrumbDto>>> getCategoryPath(
        @PathVariable("categoryId") String categoryId) {
        log.info("Fetching category path for category ID: {}", categoryId);
        
        List<CategoryBreadcrumbDto> categoryPath = categoryManagementService.getCategoryPath(
            UUID.fromString(categoryId));
        
        return ResponseBuilder.success("Category path fetched successfully", categoryPath);
    }
    
    /**
     * Validate category URL uniqueness
     */
    @GetMapping(ApiConstant.VALIDATE_CATEGORY_URL_API)
    @SwaggerOperation(
        summary = "Validate category URL",
        description = "Check if a category URL is available and valid for use")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<Boolean>> validateCategoryUrl(
        @RequestParam String categoryUrl,
        @RequestParam(required = false) UUID excludeCategoryId) {
        log.info("Validating category URL: {}, excluding ID: {}", categoryUrl, excludeCategoryId);
        
        boolean isValid = categoryManagementService.validateCategoryUrl(categoryUrl, excludeCategoryId);
        
        return ResponseBuilder.success("URL validation completed", isValid);
    }
    
    /**
     * Get root categories (categories without parent)
     */
    @GetMapping(ApiConstant.GET_ROOT_CATEGORIES_API)
    @SwaggerOperation(
        summary = "Get root categories",
        description = "Retrieve all root categories (categories without parent) for main navigation")
    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<List<CategoryInfoDto>>> getRootCategories(
        @RequestParam(required = false) String type) {
        log.info("Fetching root categories with type filter: {}", type);
        
        List<CategoryInfoDto> rootCategories = categoryManagementService.getRootCategories(type);
        
        return ResponseBuilder.success("Root categories fetched successfully", rootCategories);
    }
    
    /**
     * Update category status (isActive) only
     */
    @PostMapping(ApiConstant.UPDATE_CATEGORY_STATUS_API)
    @SwaggerOperation(
        summary = "Update category status",
        description = "Update only the active status (isActive) of a category")
    public ResponseEntity<ApiResponse<CategoryInfoDto>> updateCategoryStatus(
        @PathVariable("categoryId") String categoryId,
        @Valid @RequestBody CategoryStatusUpdateRequest statusRequest) {
        log.info("Updating category status for ID: {} to {}", categoryId, statusRequest.getIsActive());
        
        CategoryInfoDto updatedCategory = categoryManagementService.updateCategoryStatus(
            UUID.fromString(categoryId), statusRequest);
        
        return ResponseBuilder.success("Category status updated successfully", updatedCategory);
    }
    
}
