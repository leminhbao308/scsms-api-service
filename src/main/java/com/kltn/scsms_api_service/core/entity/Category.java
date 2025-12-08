package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import com.kltn.scsms_api_service.core.entity.enumAttribute.CategoryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "categories", schema = GeneralConstant.DB_SCHEMA_DEV)
public class Category extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "category_id", nullable = false)
    private UUID categoryId;
    
    @Column(name = "category_code", unique = true, nullable = true, length = 50)
    private String categoryCode;
    
    @Column(name = "category_name", nullable = false)
    private String categoryName;
    
    @Column(name = "category_url", unique = true, nullable = false, length = Integer.MAX_VALUE)
    private String categoryUrl;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;
    
    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", length = 50)
    private CategoryType categoryType;
    
    @Column(name = "level", nullable = true)
    @Builder.Default
    private Integer level = 0;
    
    @Column(name = "sort_order", nullable = true)
    @Builder.Default
    private Integer sortOrder = 0;
    
    // One-to-many relationship for subcategories
    @OneToMany(mappedBy = "parentCategory", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Category> subcategories;
}
