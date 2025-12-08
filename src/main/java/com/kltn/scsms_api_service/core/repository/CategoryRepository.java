package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.Category;
import com.kltn.scsms_api_service.core.entity.enumAttribute.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID>, JpaSpecificationExecutor<Category> {
    
    /**
     * Find category by URL slug
     */
    Optional<Category> findByCategoryUrl(String categoryUrl);
    
    /**
     * Check if category URL exists, excluding specific category ID
     */
    boolean existsByCategoryUrlAndCategoryIdNot(String categoryUrl, UUID categoryId);
    
    /**
     * Check if category URL exists
     */
    boolean existsByCategoryUrl(String categoryUrl);
    
    /**
     * Check if category code exists
     */
    boolean existsByCategoryCode(String categoryCode);
    
    /**
     * Check if category code exists, excluding specific category ID
     */
    boolean existsByCategoryCodeAndCategoryIdNot(String categoryCode, UUID categoryId);
    
    /**
     * Find all root categories (categories without parent)
     */
    List<Category> findByParentCategoryIsNull();
    
    /**
     * Find root categories by type
     */
    List<Category> findByParentCategoryIsNullAndCategoryType(CategoryType categoryType);
    
    /**
     * Find all subcategories of a parent category
     */
    List<Category> findByParentCategoryCategoryId(UUID parentCategoryId);
    
    /**
     * Find all subcategories of a parent category with subcategories loaded
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.subcategories WHERE c.parentCategory.categoryId = :parentCategoryId")
    List<Category> findByParentCategoryCategoryIdWithSubcategories(@Param("parentCategoryId") UUID parentCategoryId);
    
    /**
     * Find category by ID with subcategories loaded
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.subcategories WHERE c.categoryId = :categoryId")
    Optional<Category> findByIdWithSubcategories(@Param("categoryId") UUID categoryId);
    
    /**
     * Find category by ID with parent and subcategories loaded
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.parentCategory LEFT JOIN FETCH c.subcategories WHERE c.categoryId = :categoryId")
    Optional<Category> findByIdWithParentAndSubcategories(@Param("categoryId") UUID categoryId);
    
    /**
     * Find subcategories by parent and type
     */
    List<Category> findByParentCategoryCategoryIdAndCategoryType(UUID parentCategoryId, CategoryType categoryType);
    
    /**
     * Check if a category has subcategories
     */
    boolean existsByParentCategoryCategoryId(UUID parentCategoryId);
    
    /**
     * Check if a category has active subcategories (not deleted)
     */
    boolean existsByParentCategoryCategoryIdAndIsDeletedFalse(UUID parentCategoryId);
    
    /**
     * Count subcategories for a parent category
     */
    long countByParentCategoryCategoryId(UUID parentCategoryId);
    
    /**
     * Find categories by type
     */
    List<Category> findByCategoryType(CategoryType categoryType);
    
    /**
     * Find categories by type with pagination
     */
    Page<Category> findByCategoryType(CategoryType categoryType, Pageable pageable);
    
    /**
     * Search categories by name (case-insensitive)
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Category> findByCategoryNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Search categories by name with limit
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY c.categoryName")
    List<Category> findByCategoryNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    /**
     * Find categories for autocomplete suggestions
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.categoryName) LIKE LOWER(CONCAT(:partial, '%')) " +
        "AND (:parentId IS NULL OR c.parentCategory.categoryId = :parentId) " +
        "ORDER BY c.categoryName")
    List<Category> findCategorySuggestions(@Param("partial") String partial,
                                           @Param("parentId") UUID parentId,
                                           Pageable pageable);
    
    /**
     * Get category hierarchy up to specified depth
     */
    @Query(value = "WITH RECURSIVE category_hierarchy AS (" +
        "    SELECT c.category_id, c.category_name, c.category_url, c.parent_category_id, " +
        "           c.category_type, c.description, 0 as level, " +
        "           CAST(c.category_name AS TEXT) as path " +
        "    FROM categories c " +
        "    WHERE c.parent_category_id IS NULL " +
        "    AND (:type IS NULL OR c.category_type = :type) " +
        "    UNION ALL " +
        "    SELECT c.category_id, c.category_name, c.category_url, c.parent_category_id, " +
        "           c.category_type, c.description, ch.level + 1, " +
        "           CONCAT(ch.path, ' > ', c.category_name) " +
        "    FROM categories c " +
        "    INNER JOIN category_hierarchy ch ON c.parent_category_id = ch.category_id " +
        "    WHERE ch.level < :maxDepth " +
        ") " +
        "SELECT * FROM category_hierarchy ORDER BY path",
        nativeQuery = true)
    List<Object[]> getCategoryHierarchy(@Param("type") String type, @Param("maxDepth") int maxDepth);
    
    /**
     * Get full path for a category
     */
    @Query(value = "WITH RECURSIVE category_path AS (" +
        "    SELECT c.category_id, c.category_name, c.category_url, c.parent_category_id, " +
        "           0 as level, CAST(c.category_name AS TEXT) as path " +
        "    FROM categories c " +
        "    WHERE c.category_id = :categoryId " +
        "    UNION ALL " +
        "    SELECT p.category_id, p.category_name, p.category_url, p.parent_category_id, " +
        "           cp.level + 1, CONCAT(p.category_name, ' > ', cp.path) " +
        "    FROM categories p " +
        "    INNER JOIN category_path cp ON p.category_id = cp.parent_category_id " +
        ") " +
        "SELECT * FROM category_path ORDER BY level DESC",
        nativeQuery = true)
    List<Object[]> getCategoryPath(@Param("categoryId") UUID categoryId);
    
    /**
     * Check if moving a category would create circular reference
     */
    @Query(value = "WITH RECURSIVE category_descendants AS (" +
        "    SELECT category_id FROM categories WHERE category_id = :categoryId " +
        "    UNION ALL " +
        "    SELECT c.category_id FROM categories c " +
        "    INNER JOIN category_descendants cd ON c.parent_category_id = cd.category_id " +
        ") " +
        "SELECT COUNT(*) > 0 FROM category_descendants WHERE category_id = :potentialParentId",
        nativeQuery = true)
    boolean wouldCreateCircularReference(@Param("categoryId") UUID categoryId,
                                         @Param("potentialParentId") UUID potentialParentId);
    
    /**
     * Get category statistics
     */
    @Query("SELECT COUNT(c) FROM Category c")
    long getTotalCategoriesCount();
    
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parentCategory IS NULL")
    long getRootCategoriesCount();
    
    @Query("SELECT COUNT(c) FROM Category c WHERE c.categoryId NOT IN (SELECT DISTINCT sc.parentCategory.categoryId FROM Category sc WHERE sc.parentCategory IS NOT NULL)")
    long getLeafCategoriesCount();
    
    @Query("SELECT COUNT(c) FROM Category c WHERE c.categoryId IN (SELECT DISTINCT sc.parentCategory.categoryId FROM Category sc WHERE sc.parentCategory IS NOT NULL)")
    long getCategoriesWithSubcategoriesCount();
    
    /**
     * Get maximum hierarchy depth
     */
    @Query(value = "WITH RECURSIVE category_depth AS (" +
        "    SELECT category_id, 0 as depth FROM categories WHERE parent_category_id IS NULL " +
        "    UNION ALL " +
        "    SELECT c.category_id, cd.depth + 1 " +
        "    FROM categories c " +
        "    INNER JOIN category_depth cd ON c.parent_category_id = cd.category_id " +
        ") " +
        "SELECT COALESCE(MAX(depth), 0) FROM category_depth",
        nativeQuery = true)
    int getMaximumDepth();
    
    /**
     * Custom method to check if category belongs to specific parent
     */
    boolean existsByCategoryIdAndParentCategoryCategoryId(UUID categoryId, UUID parentCategoryId);
    
    /**
     * Find all categories that are descendants of a given category
     */
    @Query(value = "WITH RECURSIVE category_descendants AS (" +
        "    SELECT category_id, category_name, parent_category_id, 0 as level " +
        "    FROM categories WHERE parent_category_id = :parentId " +
        "    UNION ALL " +
        "    SELECT c.category_id, c.category_name, c.parent_category_id, cd.level + 1 " +
        "    FROM categories c " +
        "    INNER JOIN category_descendants cd ON c.parent_category_id = cd.category_id " +
        ") " +
        "SELECT category_id FROM category_descendants",
        nativeQuery = true)
    List<UUID> findAllDescendantIds(@Param("parentId") UUID parentId);
    
    /**
     * Delete category and all its descendants
     */
    @Modifying
    @Query("DELETE FROM Category c WHERE c.categoryId IN :categoryIds")
    void deleteAllByIdIn(@Param("categoryIds") List<UUID> categoryIds);
}
