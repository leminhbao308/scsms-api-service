package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "centers", schema = GeneralConstant.DB_SCHEMA_DEV)
public class Center extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "center_id", nullable = false)
    private UUID centerId;
    
    @Column(name = "center_name", nullable = false)
    private String centerName;
    
    @Column(name = "center_code", unique = true, nullable = false)
    private String centerCode;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(name = "headquarters_address", length = Integer.MAX_VALUE)
    private String headquartersAddress;
    
    @Column(name = "headquarters_phone")
    private String headquartersPhone;
    
    @Column(name = "headquarters_email")
    private String headquartersEmail;
    
    @Column(name = "website")
    private String website;
    
    @Column(name = "tax_code", unique = true)
    private String taxCode;
    
    @Column(name = "business_license")
    private String businessLicense;
    
    @Column(name = "logo_url", length = Integer.MAX_VALUE)
    private String logoUrl;
    
    @Column(name = "established_date")
    private java.time.LocalDate establishedDate;
    
    
    // One-to-many relationship with branches
    @OneToMany(mappedBy = "center", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Branch> branches = new HashSet<>();
    
    // One-to-many relationship with business hours
    @OneToMany(mappedBy = "center", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<CenterBusinessHours> businessHours = new HashSet<>();
    
    // One-to-many relationship with social media
    @OneToMany(mappedBy = "center", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<CenterSocialMedia> socialMedia = new HashSet<>();
    
    // Many-to-one relationship with manager (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;
    
    @Column(name = "manager_assigned_at")
    private java.time.LocalDateTime managerAssignedAt;
    
    @Column(name = "manager_assigned_by")
    private String managerAssignedBy;
    
    
    // Operating status
    @Column(name = "operating_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OperatingStatus operatingStatus = OperatingStatus.ACTIVE;
    
    public enum OperatingStatus {
        ACTIVE, INACTIVE, SUSPENDED, MAINTENANCE
    }
    
    // Utility methods
    public void addBranch(Branch branch) {
        branches.add(branch);
        if (branch != null) {
            branch.setCenter(this);
        }
    }
    
    public void removeBranch(Branch branch) {
        branches.remove(branch);
        if (branch != null) {
            branch.setCenter(null);
        }
    }
    
    public void setManager(User manager) {
        this.manager = manager;
        this.managerAssignedAt = java.time.LocalDateTime.now();
        this.managerAssignedBy = "SYSTEM";
    }
}
