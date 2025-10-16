package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.dto.categoryManagement.CategoryBreadcrumbDto;
import com.kltn.scsms_api_service.core.dto.categoryManagement.CategoryHierarchyDto;
import com.kltn.scsms_api_service.core.dto.categoryManagement.CategoryInfoDto;
import com.kltn.scsms_api_service.core.dto.categoryManagement.param.CategoryFilterParam;
import com.kltn.scsms_api_service.core.dto.categoryManagement.request.CategoryCreateRequest;
import com.kltn.scsms_api_service.core.dto.categoryManagement.request.CategoryUpdateRequest;
import com.kltn.scsms_api_service.core.entity.Category;
import com.kltn.scsms_api_service.core.entity.enumAttribute.CategoryType;
import com.kltn.scsms_api_service.core.repository.CategoryRepository;
import com.kltn.scsms_api_service.core.service.businessService.CategoryManagementService.CategoryStatsDto;
import com.kltn.scsms_api_service.mapper.CategoryMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EntityManager entityManager;
    
    /**
     * Get all categories with filters and pagination
     */
    public Page<CategoryInfoDto> getAllCategories(CategoryFilterParam filterParam) {
        log.debug("Getting categories with filters: {}", filterParam);
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Category> query = cb.createQuery(Category.class);
        Root<Category> categoryRoot = query.from(Category.class);
        
        // Left join with parent category for filtering and response mapping
        Join<Category, Category> parentJoin = categoryRoot.join("parentCategory", JoinType.LEFT);
        // Left join with subcategories for counting
        // Join<Category, Category> subcategoriesJoin = categoryRoot.join("subcategories", JoinType.LEFT);
        
        List<Predicate> predicates = buildPredicates(cb, categoryRoot, parentJoin, filterParam);
        
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        // Apply sorting
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(filterParam.getDirection())
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Order order = buildOrderClause(cb, categoryRoot, parentJoin, filterParam.getSort(), sortDirection);
        query.orderBy(order);
        
        // Group by to handle joins properly
        query.groupBy(categoryRoot.get("categoryId"));
        
        TypedQuery<Category> typedQuery = entityManager.createQuery(query);
        
        // Get total count for pagination
        long totalElements = getTotalCount(filterParam);
        
        // Apply pagination
        int offset = filterParam.getPage() * filterParam.getSize();
        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(filterParam.getSize());
        
        List<Category> categories = typedQuery.getResultList();
        
        // Convert to DTOs using mapper
        List<CategoryInfoDto> categoryDtos = categories.stream()
            .map(categoryMapper::toDetailedInfoDto)
            .collect(Collectors.toList());
        
        PageRequest pageRequest = PageRequest.of(filterParam.getPage(), filterParam.getSize());
        return new PageImpl<>(categoryDtos, pageRequest, totalElements);
    }
    
    /**
     * Get category hierarchy
     */
    public List<CategoryHierarchyDto> getCategoryHierarchy(String type, UUID parentId, int maxDepth) {
        log.debug("Getting category hierarchy - type: {}, parentId: {}, maxDepth: {}", type, parentId, maxDepth);
        
        List<Category> categories;
        
        if (parentId != null) {
            // Get all categories under the parent to build proper hierarchy
            categoryRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("Parent category not found: " + parentId));
            categories = categoryRepository.findAll();
            return categoryMapper.buildHierarchyTree(categories).stream()
                .filter(dto -> dto.getParentId().equals(parentId))
                .collect(Collectors.toList());
        } else if (type != null) {
            // Get categories of specific type
            CategoryType categoryType = CategoryType.valueOf(type.toUpperCase());
            categories = categoryRepository.findByCategoryType(categoryType);
        } else {
            // Get all categories
            categories = categoryRepository.findAll();
        }
        
        return categoryMapper.buildHierarchyTree(categories);
    }
    
    /**
     * Get all categories with breadcrumb information
     */
    public List<CategoryBreadcrumbDto> getAllCategoriesWithBreadcrumb(String type, UUID parentId) {
        log.debug("Getting categories with breadcrumb - type: {}, parentId: {}", type, parentId);
        
        List<Category> categories;
        
        if (parentId != null) {
            categories = categoryRepository.findByParentCategoryCategoryId(parentId);
        } else if (type != null) {
            CategoryType categoryType = CategoryType.valueOf(type.toUpperCase());
            categories = categoryRepository.findByCategoryType(categoryType);
        } else {
            categories = categoryRepository.findAll();
        }
        
        return categoryMapper.toBreadcrumbDtoList(categories);
    }
    
    /**
     * Get category by ID
     */
    public CategoryInfoDto getCategoryById(UUID categoryId) {
        log.debug("Getting category by ID: {}", categoryId);
        
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + categoryId));
        
        return categoryMapper.toDetailedInfoDto(category);
    }
    
    /**
     * Get category by ID with subcategories loaded (for better performance)
     */
    public CategoryInfoDto getCategoryByIdWithSubcategories(UUID categoryId) {
        log.debug("Getting category by ID with subcategories: {}", categoryId);
        
        Category category = categoryRepository.findByIdWithSubcategories(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + categoryId));
        
        return categoryMapper.toDetailedInfoDto(category);
    }
    
    /**
     * Get category by ID with parent and subcategories loaded (for complete hierarchy)
     */
    public CategoryInfoDto getCategoryByIdWithFullHierarchy(UUID categoryId) {
        log.debug("Getting category by ID with full hierarchy: {}", categoryId);
        
        Category category = categoryRepository.findByIdWithParentAndSubcategories(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + categoryId));
        
        return categoryMapper.toDetailedInfoDto(category);
    }
    
    /**
     * Get category by URL
     */
    public CategoryInfoDto getCategoryByUrl(String categoryUrl) {
        log.debug("Getting category by URL: {}", categoryUrl);
        
        Category category = categoryRepository.findByCategoryUrl(categoryUrl)
            .orElseThrow(() -> new EntityNotFoundException("Category not found with URL: " + categoryUrl));
        
        return categoryMapper.toDetailedInfoDto(category);
    }
    
    /**
     * Get subcategories
     */
    public List<CategoryInfoDto> getSubcategories(UUID categoryId, boolean includeInactive) {
        log.debug("Getting subcategories for category: {}, includeInactive: {}", categoryId, includeInactive);
        
        categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Parent category not found: " + categoryId));
        
        List<Category> subcategories = categoryRepository.findByParentCategoryCategoryId(categoryId);
        
        if (!includeInactive) {
            subcategories = subcategories.stream()
                .filter(cat -> !cat.getIsDeleted())
                .toList();
        }
        
        return subcategories.stream()
            .map(categoryMapper::toDetailedInfoDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Create category
     */
    @Transactional
    public CategoryInfoDto createCategory(CategoryCreateRequest createRequest) {
        log.info("Creating category: {}", createRequest.getCategoryName());
        
        // Use mapper to convert request to entity
        Category category = categoryMapper.toEntity(createRequest);
        
        // Set parent category if provided
        if (createRequest.getParentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(createRequest.getParentCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Parent category not found: " + createRequest.getParentCategoryId()));
            category.setParentCategory(parentCategory);
            
            // Set level based on parent (handle null parent level)
            Integer parentLevel = parentCategory.getLevel();
            category.setLevel(parentLevel != null ? parentLevel + 1 : 1);
        } else {
            // Root category
            category.setLevel(0);
        }
        
        // Set sort order if not provided
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        
        // Set category code if not provided
        if (category.getCategoryCode() == null || category.getCategoryCode().trim().isEmpty()) {
            // Generate a default category code
            category.setCategoryCode("CAT_" + System.currentTimeMillis());
        }
        
        // Ensure is_active and is_deleted are never null (fallback protection)
        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }
        if (category.getIsDeleted() == null) {
            category.setIsDeleted(false);
        }
        
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toDetailedInfoDto(savedCategory);
    }
    
    /**
     * Update category
     */
    @Transactional
    public CategoryInfoDto updateCategory(UUID categoryId, CategoryUpdateRequest updateRequest) {
        log.info("Updating category: {}", categoryId);
        
        Category existingCategory = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + categoryId));
        
        // Use mapper to update entity from request
        categoryMapper.updateEntityFromRequest(updateRequest, existingCategory);
        
        // Ensure is_active and is_deleted are never null (fallback protection)
        if (existingCategory.getIsActive() == null) {
            existingCategory.setIsActive(true);
        }
        if (existingCategory.getIsDeleted() == null) {
            existingCategory.setIsDeleted(false);
        }
        
        // Update parent category if provided
        if (updateRequest.getParentCategoryId() != null) {
            Category newParent = categoryRepository.findById(updateRequest.getParentCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Parent category not found: " + updateRequest.getParentCategoryId()));
            existingCategory.setParentCategory(newParent);
            
            // Update level based on new parent (handle null parent level)
            Integer parentLevel = newParent.getLevel();
            existingCategory.setLevel(parentLevel != null ? parentLevel + 1 : 1);
        } else if (updateRequest.getParentCategoryId() == null && existingCategory.getParentCategory() != null) {
            // Moving to root level
            existingCategory.setParentCategory(null);
            existingCategory.setLevel(0);
        }
        
        // Note: Level should be calculated automatically based on parent hierarchy
        // Manual level updates are not allowed to maintain hierarchy integrity
        
        // Update sort order if provided
        if (updateRequest.getSortOrder() != null) {
            existingCategory.setSortOrder(updateRequest.getSortOrder());
        }
        
        Category updatedCategory = categoryRepository.save(existingCategory);
        return categoryMapper.toDetailedInfoDto(updatedCategory);
    }
    
    /**
     * Delete category (soft delete only)
     */
    @Transactional
    public void deleteCategory(UUID categoryId, boolean force) {
        log.info("Soft deleting category: {}", categoryId);
        
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + categoryId));
        
        // Always soft delete - just mark as deleted
        category.setIsDeleted(true);
        category.setIsActive(false); // Also deactivate when deleted
        categoryRepository.save(category);
    }
    
    /**
     * Move category to different parent
     */
    @Transactional
    public CategoryInfoDto moveCategory(UUID categoryId, UUID newParentId) {
        log.info("Moving category {} to parent: {}", categoryId, newParentId);
        
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + categoryId));
        
        Category newParent = null;
        if (newParentId != null) {
            newParent = categoryRepository.findById(newParentId)
                .orElseThrow(() -> new EntityNotFoundException("New parent category not found: " + newParentId));
        }
        
        category.setParentCategory(newParent);
        Category movedCategory = categoryRepository.save(category);
        
        return categoryMapper.toDetailedInfoDto(movedCategory);
    }
    
    /**
     * Get category path
     */
    public List<CategoryBreadcrumbDto> getCategoryPath(UUID categoryId) {
        log.debug("Getting category path for: {}", categoryId);
        
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + categoryId));
        
        return categoryMapper.buildBreadcrumb(category);
    }
    
    /**
     * Check if category URL is unique
     */
    public boolean isCategoryUrlUnique(String categoryUrl, UUID excludeCategoryId) {
        if (excludeCategoryId != null) {
            return !categoryRepository.existsByCategoryUrlAndCategoryIdNot(categoryUrl, excludeCategoryId);
        } else {
            return !categoryRepository.existsByCategoryUrl(categoryUrl);
        }
    }
    
    /**
     * Check if category code is unique
     */
    public boolean isCategoryCodeUnique(String categoryCode, UUID excludeCategoryId) {
        if (excludeCategoryId != null) {
            return !categoryRepository.existsByCategoryCodeAndCategoryIdNot(categoryCode, excludeCategoryId);
        } else {
            return !categoryRepository.existsByCategoryCode(categoryCode);
        }
    }
    
    /**
     * Get root categories
     */
    public List<CategoryInfoDto> getRootCategories(String type) {
        log.debug("Getting root categories with type: {}", type);
        
        List<Category> rootCategories;
        if (type != null) {
            CategoryType categoryType = CategoryType.valueOf(type.toUpperCase());
            rootCategories = categoryRepository.findByParentCategoryIsNullAndCategoryType(categoryType);
        } else {
            rootCategories = categoryRepository.findByParentCategoryIsNull();
        }
        
        return rootCategories.stream()
            .map(categoryMapper::toDetailedInfoDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Check if category exists
     */
    public boolean existsById(UUID categoryId) {
        return categoryRepository.existsById(categoryId);
    }
    
    /**
     * Check if would create circular reference
     */
    public boolean wouldCreateCircularReference(UUID categoryId, UUID potentialParentId) {
        return categoryRepository.wouldCreateCircularReference(categoryId, potentialParentId);
    }
    
    /**
     * Check if category has subcategories
     */
    public boolean hasSubcategories(UUID categoryId) {
        return categoryRepository.existsByParentCategoryCategoryId(categoryId);
    }
    
    /**
     * Check if category has active subcategories (not deleted)
     */
    public boolean hasActiveSubcategories(UUID categoryId) {
        return categoryRepository.existsByParentCategoryCategoryIdAndIsDeletedFalse(categoryId);
    }
    
    /**
     * Get subcategory count for a category (using repository query for better performance)
     */
    public long getSubcategoryCount(UUID categoryId) {
        return categoryRepository.countByParentCategoryCategoryId(categoryId);
    }
    
    /**
     * Check if category belongs to parent
     */
    public boolean belongsToParent(UUID categoryId, UUID parentId) {
        if (parentId == null) {
            // Check if category is root category
            return categoryRepository.findById(categoryId)
                .map(category -> category.getParentCategory() == null)
                .orElse(false);
        }
        return categoryRepository.existsByCategoryIdAndParentCategoryCategoryId(categoryId, parentId);
    }
    
    /**
     * Get category statistics
     */
    public CategoryStatsDto getCategoryStatistics() {
        log.debug("Getting category statistics");
        
        long totalCategories = categoryRepository.getTotalCategoriesCount();
        long rootCategories = categoryRepository.getRootCategoriesCount();
        long leafCategories = categoryRepository.getLeafCategoriesCount();
        long categoriesWithSubcategories = categoryRepository.getCategoriesWithSubcategoriesCount();
        int maxDepth = categoryRepository.getMaximumDepth();
        
        return new CategoryStatsDto(totalCategories, rootCategories, leafCategories, maxDepth, categoriesWithSubcategories);
    }
    
    /**
     * Bulk create categories
     */
    @Transactional
    public List<CategoryInfoDto> bulkCreateCategories(List<CategoryCreateRequest> createRequests) {
        log.info("Bulk creating {} categories", createRequests.size());
        
        List<Category> categories = createRequests.stream()
            .map(request -> {
                Category category = categoryMapper.toEntity(request);
                
                if (request.getParentCategoryId() != null) {
                    Category parent = categoryRepository.getReferenceById(request.getParentCategoryId());
                    category.setParentCategory(parent);
                    // Set level based on parent
                    Integer parentLevel = parent.getLevel();
                    category.setLevel(parentLevel != null ? parentLevel + 1 : 1);
                } else {
                    category.setLevel(0);
                }
                
                // Ensure is_active and is_deleted are never null (fallback protection)
                if (category.getIsActive() == null) {
                    category.setIsActive(true);
                }
                if (category.getIsDeleted() == null) {
                    category.setIsDeleted(false);
                }
                
                return category;
            })
            .collect(Collectors.toList());
        
        List<Category> savedCategories = categoryRepository.saveAll(categories);
        
        return savedCategories.stream()
            .map(categoryMapper::toDetailedInfoDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Reorder categories
     */
    @Transactional
    public List<CategoryInfoDto> reorderCategories(UUID parentId, List<UUID> categoryIds) {
        log.info("Reordering categories under parent: {}", parentId);
        
        // This would require an order field in Category entity
        // For now, return the categories in the requested order
        List<Category> categories = categoryIds.stream()
            .map(id -> categoryRepository.findById(id).orElse(null))
            .filter(Objects::nonNull)
            .toList();
        
        return categories.stream()
            .map(categoryMapper::toDetailedInfoDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Search categories
     */
    public List<CategoryInfoDto> searchCategories(String searchTerm, int limit) {
        log.debug("Searching categories with term: {}, limit: {}", searchTerm, limit);
        
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<Category> categories = categoryRepository.findByCategoryNameContainingIgnoreCase(searchTerm, pageRequest);
        
        return categories.stream()
            .map(categoryMapper::toDetailedInfoDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Get category suggestions
     */
    public List<CategoryInfoDto> getCategorySuggestions(String partial, UUID parentId, int limit) {
        log.debug("Getting category suggestions for: {}, parent: {}, limit: {}", partial, parentId, limit);
        
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<Category> categories = categoryRepository.findCategorySuggestions(partial, parentId, pageRequest);
        
        return categories.stream()
            .map(categoryMapper::toDetailedInfoDto)
            .collect(Collectors.toList());
    }
    
    // Private helper methods
    
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Category> categoryRoot,
                                            Join<Category, Category> parentJoin, CategoryFilterParam filterParam) {
        List<Predicate> predicates = new ArrayList<>();
        
        // Active filter (not deleted)
        if (filterParam.getActive() != null) {
            predicates.add(cb.equal(categoryRoot.get("isDeleted"), !filterParam.getActive()));
        } else {
            // By default, exclude deleted categories
            predicates.add(cb.equal(categoryRoot.get("isDeleted"), false));
        }
        
        // Type filter
        if (filterParam.getCategoryType() != null) {
            predicates.add(cb.equal(categoryRoot.get("categoryType"), filterParam.getCategoryType()));
        }
        
        // Parent category filter
        if (filterParam.getParentCategoryId() != null) {
            predicates.add(cb.equal(parentJoin.get("categoryId"), filterParam.getParentCategoryId()));
        }
        
        // Has parent filter
        if (filterParam.getHasParent() != null) {
            if (filterParam.getHasParent()) {
                predicates.add(cb.isNotNull(categoryRoot.get("parentCategory")));
            } else {
                predicates.add(cb.isNull(categoryRoot.get("parentCategory")));
            }
        }
        
        // Root only filter
        if (Boolean.TRUE.equals(filterParam.getRootOnly())) {
            predicates.add(cb.isNull(categoryRoot.get("parentCategory")));
        }
        
        // Has subcategories filter
        if (filterParam.getHasSubcategories() != null) {
            if (filterParam.getHasSubcategories()) {
                predicates.add(cb.isNotEmpty(categoryRoot.get("subcategories")));
            } else {
                predicates.add(cb.isEmpty(categoryRoot.get("subcategories")));
            }
        }
        
        // Leaf only filter
        if (Boolean.TRUE.equals(filterParam.getLeafOnly())) {
            predicates.add(cb.isEmpty(categoryRoot.get("subcategories")));
        }
        
        // Search filters
        if (filterParam.getSearch() != null) {
            String searchPattern = "%" + filterParam.getSearch().toLowerCase() + "%";
            Predicate searchPredicate = cb.or(
                cb.like(cb.lower(categoryRoot.get("categoryName")), searchPattern),
                cb.like(cb.lower(categoryRoot.get("categoryUrl")), searchPattern),
                cb.like(cb.lower(categoryRoot.get("description")), searchPattern)
            );
            predicates.add(searchPredicate);
        }
        
        if (filterParam.getCategoryName() != null) {
            predicates.add(cb.like(cb.lower(categoryRoot.get("categoryName")),
                "%" + filterParam.getCategoryName().toLowerCase() + "%"));
        }
        
        if (filterParam.getCategoryUrl() != null) {
            predicates.add(cb.like(cb.lower(categoryRoot.get("categoryUrl")),
                "%" + filterParam.getCategoryUrl().toLowerCase() + "%"));
        }
        
        if (filterParam.getDescription() != null) {
            predicates.add(cb.like(cb.lower(categoryRoot.get("description")),
                "%" + filterParam.getDescription().toLowerCase() + "%"));
        }
        
        if (filterParam.getParentCategoryName() != null) {
            predicates.add(cb.like(cb.lower(parentJoin.get("categoryName")),
                "%" + filterParam.getParentCategoryName().toLowerCase() + "%"));
        }
        
        // Date range filters
        addDateRangePredicates(cb, categoryRoot, predicates, filterParam);
        
        return predicates;
    }
    
    private void addDateRangePredicates(CriteriaBuilder cb, Root<Category> categoryRoot,
                                        List<Predicate> predicates, CategoryFilterParam filterParam) {
        // Created date range
        if (filterParam.getCreatedDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(categoryRoot.get("createDate"), filterParam.getCreatedDateFrom()));
        }
        if (filterParam.getCreatedDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(categoryRoot.get("createDate"), filterParam.getCreatedDateTo()));
        }
        
        // Updated date range
        if (filterParam.getModifiedDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(categoryRoot.get("modifiedDate"), filterParam.getModifiedDateFrom()));
        }
        if (filterParam.getModifiedDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(categoryRoot.get("modifiedDate"), filterParam.getModifiedDateTo()));
        }
    }
    
    private Order buildOrderClause(CriteriaBuilder cb, Root<Category> categoryRoot,
                                   Join<Category, Category> parentJoin, String sortField, Sort.Direction direction) {
        Expression<?> sortExpression = switch (sortField) {
            case "parentCategory.categoryName", "parent.categoryName" -> parentJoin.get("categoryName");
            case "parentCategory.categoryUrl", "parent.categoryUrl" -> parentJoin.get("categoryUrl");
            case "subcategoryCount" -> cb.size(categoryRoot.get("subcategories"));
            default -> categoryRoot.get(sortField);
        };
        
        return direction == Sort.Direction.ASC
            ? cb.asc(sortExpression)
            : cb.desc(sortExpression);
    }
    
    private long getTotalCount(CategoryFilterParam filterParam) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Category> categoryRoot = countQuery.from(Category.class);
        Join<Category, Category> parentJoin = categoryRoot.join("parentCategory", JoinType.LEFT);
        
        countQuery.select(cb.countDistinct(categoryRoot.get("categoryId")));
        
        List<Predicate> predicates = buildPredicates(cb, categoryRoot, parentJoin, filterParam);
        if (!predicates.isEmpty()) {
            countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        return entityManager.createQuery(countQuery).getSingleResult();
    }
    
    /**
     * Get category reference by ID (for lazy loading)
     */
    public Category getCategoryRefById(UUID categoryId) {
        return categoryRepository.getReferenceById(categoryId);
    }
    
    /**
     * Find category by ID (optional)
     */
    public Optional<Category> findById(UUID categoryId) {
        return categoryRepository.findById(categoryId);
    }
    
    /**
     * Get category by ID (required)
     */
    public Category getById(UUID categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + categoryId));
    }
    
    /**
     * Find category by URL (optional)
     */
    public Optional<Category> findByUrl(String categoryUrl) {
        return categoryRepository.findByCategoryUrl(categoryUrl);
    }
    
    /**
     * Save category
     */
    @Transactional
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }
    
    /**
     * Get categories by type
     */
    public List<CategoryInfoDto> getCategoriesByType(CategoryType type) {
        List<Category> categories = categoryRepository.findByCategoryType(type);
        return categories.stream()
            .map(categoryMapper::toDetailedInfoDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Get categories by type with pagination
     */
    public Page<CategoryInfoDto> getCategoriesByType(CategoryType type, int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<Category> categoryPage = categoryRepository.findByCategoryType(type, pageRequest);
        
        List<CategoryInfoDto> categoryDtos = categoryPage.getContent().stream()
            .map(categoryMapper::toDetailedInfoDto)
            .collect(Collectors.toList());
        
        return new PageImpl<>(categoryDtos, pageRequest, categoryPage.getTotalElements());
    }
    
    /**
     * Count subcategories for a category
     */
    public long countSubcategories(UUID categoryId) {
        return categoryRepository.countByParentCategoryCategoryId(categoryId);
    }
    
    /**
     * Get all descendants of a category (all levels)
     */
    public List<CategoryInfoDto> getAllDescendants(UUID categoryId) {
        List<UUID> descendantIds = categoryRepository.findAllDescendantIds(categoryId);
        List<Category> descendants = categoryRepository.findAllById(descendantIds);
        
        return descendants.stream()
            .map(categoryMapper::toDetailedInfoDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Validate category hierarchy constraints
     */
    public void validateHierarchyConstraints(UUID categoryId, UUID newParentId) {
        // Prevent self-parent assignment
        if (categoryId.equals(newParentId)) {
            throw new IllegalArgumentException("Category cannot be its own parent");
        }
        
        // Prevent circular references
        if (wouldCreateCircularReference(categoryId, newParentId)) {
            throw new IllegalArgumentException("Operation would create circular reference in category hierarchy");
        }
        
        // Check maximum depth (if you have a business rule for maximum depth)
        if (newParentId != null && calculateDepthFromRoot(newParentId) >= getMaxAllowedDepth()) {
            throw new IllegalArgumentException("Operation would exceed maximum allowed hierarchy depth");
        }
    }
    
    /**
     * Calculate depth from root for a category
     */
    private int calculateDepthFromRoot(UUID categoryId) {
        int depth = 0;
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        
        while (categoryOpt.isPresent() && categoryOpt.get().getParentCategory() != null) {
            depth++;
            categoryOpt = Optional.of(categoryOpt.get().getParentCategory());
        }
        
        return depth;
    }
    
    /**
     * Get maximum allowed hierarchy depth (can be configured)
     */
    private int getMaxAllowedDepth() {
        return 10; // Default maximum depth, can be made configurable
    }
    
    /**
     * Get category tree starting from a specific category
     */
    public CategoryHierarchyDto getCategoryTree(UUID categoryId, int maxDepth) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + categoryId));
        
        // Get all categories to build the tree properly
        List<Category> allCategories = categoryRepository.findAll();
        return categoryMapper.buildHierarchyNode(category, allCategories, 0);
    }
    
    /**
     * Get siblings of a category (categories with same parent)
     */
    public List<CategoryInfoDto> getSiblings(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + categoryId));
        
        List<Category> siblings;
        if (category.getParentCategory() != null) {
            siblings = categoryRepository.findByParentCategoryCategoryId(category.getParentCategory().getCategoryId());
            // Remove the category itself from siblings
            siblings = siblings.stream()
                .filter(sibling -> !sibling.getCategoryId().equals(categoryId))
                .collect(Collectors.toList());
        } else {
            // For root categories, get other root categories
            siblings = categoryRepository.findByParentCategoryIsNull();
            siblings = siblings.stream()
                .filter(sibling -> !sibling.getCategoryId().equals(categoryId))
                .collect(Collectors.toList());
        }
        
        return siblings.stream()
            .map(categoryMapper::toDetailedInfoDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Check if category name is unique within same parent
     */
    public boolean isCategoryNameUniqueInParent(String categoryName, UUID parentCategoryId, UUID excludeCategoryId) {
        // This would require a custom query in the repository
        // For now, we'll implement a simple check
        List<Category> siblings;
        if (parentCategoryId != null) {
            siblings = categoryRepository.findByParentCategoryCategoryId(parentCategoryId);
        } else {
            siblings = categoryRepository.findByParentCategoryIsNull();
        }
        
        return siblings.stream()
            .filter(category -> excludeCategoryId == null || !category.getCategoryId().equals(excludeCategoryId))
            .noneMatch(category -> category.getCategoryName().equalsIgnoreCase(categoryName));
    }
    
    /**
     * Get popular/featured categories (categories with most subcategories or most usage)
     */
    public List<CategoryInfoDto> getPopularCategories(int limit) {
        // This is a simplified implementation - you might want to implement based on actual usage metrics
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<Category> categories = categoryRepository.findAll(pageRequest).getContent();
        
        // Sort by number of subcategories (as a proxy for popularity)
        categories.sort((c1, c2) -> Integer.compare(
            c2.getSubcategories() != null ? c2.getSubcategories().size() : 0,
            c1.getSubcategories() != null ? c1.getSubcategories().size() : 0
        ));
        
        return categories.stream()
            .limit(limit)
            .map(categoryMapper::toDetailedInfoDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Get recently created categories
     */
    public List<CategoryInfoDto> getRecentCategories(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createDate"));
        Page<Category> categoryPage = categoryRepository.findAll(pageRequest);
        
        return categoryPage.getContent().stream()
            .map(categoryMapper::toDetailedInfoDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Export category hierarchy for backup or migration
     */
    public List<CategoryInfoDto> exportCategoryHierarchy() {
        List<Category> allCategories = categoryRepository.findAll(Sort.by("createDate"));
        return allCategories.stream()
            .map(categoryMapper::toDetailedInfoDto)
            .collect(Collectors.toList());
    }
    
    
    /**
     * Update category status (isActive) only
     */
    @Transactional
    public CategoryInfoDto updateCategoryStatus(UUID categoryId, Boolean isActive) {
        log.info("Updating category status: {} to {}", categoryId, isActive);
        
        Category existingCategory = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + categoryId));
        
        existingCategory.setIsActive(isActive);
        Category updatedCategory = categoryRepository.save(existingCategory);
        
        return categoryMapper.toDetailedInfoDto(updatedCategory);
    }
    
    /**
     * Validate category before save
     */
    private void validateCategory(Category category) {
        // Validate required fields
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }
        
        if (category.getCategoryUrl() == null || category.getCategoryUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Category URL is required");
        }
        
        // Validate URL format
        String url = category.getCategoryUrl().trim().toLowerCase();
        if (!url.matches("^[a-z0-9\\-_/]+$")) {
            throw new IllegalArgumentException("Category URL contains invalid characters");
        }
        
        // Check URL uniqueness
        if (!isCategoryUrlUnique(category.getCategoryUrl(), category.getCategoryId())) {
            throw new IllegalArgumentException("Category URL already exists: " + category.getCategoryUrl());
        }
        
        // Validate parent-child relationship
        if (category.getParentCategory() != null) {
            validateHierarchyConstraints(category.getCategoryId(), category.getParentCategory().getCategoryId());
        }
    }
}
