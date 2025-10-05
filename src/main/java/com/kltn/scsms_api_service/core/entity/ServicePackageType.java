package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Entity representing Service Package Type
 * Manages different types of service packages like BASIC, PREMIUM, ANNUAL, PROMO, etc.
 */
@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "service_package_types", schema = GeneralConstant.DB_SCHEMA_DEV)
public class ServicePackageType extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "service_package_type_id", nullable = false)
    private UUID servicePackageTypeId;
    
    @NotBlank(message = "Service package type code is required")
    @Size(max = 50, message = "Service package type code must not exceed 50 characters")
    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;
    
    @NotBlank(message = "Service package type name is required")
    @Size(max = 100, message = "Service package type name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Size(max = 255, message = "Price policy must not exceed 255 characters")
    @Column(name = "price_policy", length = 255)
    private String pricePolicy;
    
    @Size(max = 100, message = "Applicable customer type must not exceed 100 characters")
    @Column(name = "applicable_customer_type", length = 100)
    private String applicableCustomerType;
    
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    // Business methods
    /**
     * Check if service package type is available for use
     */
    public boolean isAvailable() {
        return isActive && !getIsDeleted();
    }
    
    /**
     * Activate service package type
     */
    public void activate() {
        this.isActive = true;
    }
    
    /**
     * Deactivate service package type
     */
    public void deactivate() {
        this.isActive = false;
    }
    
    /**
     * Set as default package type
     */
    public void setAsDefault() {
        this.isDefault = true;
    }
    
    /**
     * Remove default status
     */
    public void removeDefaultStatus() {
        this.isDefault = false;
    }
    
    /**
     * Get display name for UI
     */
    public String getDisplayName() {
        return name + " (" + code + ")";
    }
    
    /**
     * Check if this package type is applicable for customer type
     */
    public boolean isApplicableForCustomerType(String customerType) {
        if (applicableCustomerType == null || applicableCustomerType.trim().isEmpty()) {
            return true; // No restriction
        }
        return applicableCustomerType.equalsIgnoreCase(customerType);
    }
}
