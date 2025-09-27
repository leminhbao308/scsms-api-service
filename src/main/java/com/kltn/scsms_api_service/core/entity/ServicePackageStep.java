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
@Table(name = "service_package_steps", schema = GeneralConstant.DB_SCHEMA_DEV)
public class ServicePackageStep extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "package_step_id", nullable = false)
    private UUID packageStepId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private ServicePackage servicePackage;
    
    @Column(name = "step_name", nullable = false, length = Integer.MAX_VALUE)
    private String stepName;
    
    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;
    
    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;
    
    @Column(name = "step_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private StepType stepType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referenced_service_id")
    private Service referencedService;
    
    @Column(name = "estimated_duration")
    private Integer estimatedDuration; // in minutes
    
    @Column(name = "instructions", length = Integer.MAX_VALUE)
    private String instructions;
    
    @Column(name = "is_optional")
    @Builder.Default
    private Boolean isOptional = false;
    
    @Column(name = "is_parallel")
    @Builder.Default
    private Boolean isParallel = false;
    
    @Column(name = "prerequisite_steps")
    private String prerequisiteSteps; // JSON array of step orders that must be completed first
    
    @Column(name = "required_skill_level")
    @Enumerated(EnumType.STRING)
    private Service.SkillLevel requiredSkillLevel;
    
    @Column(name = "required_tools")
    private String requiredTools; // JSON array of required tools
    
    @Column(name = "safety_notes", length = Integer.MAX_VALUE)
    private String safetyNotes;
    
    @Column(name = "quality_criteria", length = Integer.MAX_VALUE)
    private String qualityCriteria;
    
    @Column(name = "photo_required")
    @Builder.Default
    private Boolean photoRequired = false;
    
    @Column(name = "customer_approval_required")
    @Builder.Default
    private Boolean customerApprovalRequired = false;
    
    // Enums
    public enum StepType {
        SERVICE, CUSTOM_STEP
    }
}
