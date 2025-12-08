package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_types", schema = GeneralConstant.DB_SCHEMA_DEV)
public class ProductType extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_type_id", nullable = false)
    private UUID productTypeId;
    
    @Column(name = "product_type_name", nullable = false, length = 255)
    private String productTypeName;
    
    @Column(name = "product_type_code", unique = true, nullable = false, length = 50)
    private String productTypeCode;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    // Utility methods
    public void setCategory(Category category) {
        this.category = category;
    }
}
