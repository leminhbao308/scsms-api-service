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
 * Entity representing Service Type
 * Manages different types of services like MAINTENANCE, CLEANING, INTERIOR, etc.
 */
@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "service_types", schema = GeneralConstant.DB_SCHEMA_DEV)
public class ServiceType extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "service_type_id", nullable = false)
    private UUID serviceTypeId;
    
    @NotBlank(message = "Service type code is required")
    @Size(max = 50, message = "Service type code must not exceed 50 characters")
    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;
    
    @NotBlank(message = "Service type name is required")
    @Size(max = 100, message = "Service type name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    // Business methods
    /**
     * Check if service type is available for use
     */
    public boolean isAvailable() {
        return getIsActive() && !getIsDeleted();
    }
    
    /**
     * Activate service type
     */
    public void activate() {
        setIsActive(true);
    }
    
    /**
     * Deactivate service type
     */
    public void deactivate() {
        setIsActive(false);
    }
    
    /**
     * Get display name for UI
     */
    public String getDisplayName() {
        return name + " (" + code + ")";
    }
}
