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
@Table(name = "branches", schema = GeneralConstant.DB_SCHEMA_DEV)
public class Branch extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    
    @Column(name = "branch_name", nullable = false)
    private String branchName;
    
    @Column(name = "branch_code", unique = true, nullable = false)
    private String branchCode;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(name = "address", length = Integer.MAX_VALUE, nullable = false)
    private String address;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "email")
    private String email;
    
    
    @Column(name = "service_capacity")
    @Builder.Default
    private Integer serviceCapacity = 10;
    
    
    
    @Column(name = "area_sqm")
    private Double areaSqm;
    
    @Column(name = "parking_spaces")
    @Builder.Default
    private Integer parkingSpaces = 0;
    
    @Column(name = "established_date")
    private java.time.LocalDate establishedDate;
    
    
    // Many-to-one relationship with center
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id", nullable = false)
    private Center center;
    
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
        ACTIVE, INACTIVE, SUSPENDED, MAINTENANCE, CLOSED
    }
    
    
    // Utility methods
    public void setManager(User manager) {
        this.manager = manager;
        this.managerAssignedAt = java.time.LocalDateTime.now();
        this.managerAssignedBy = "SYSTEM";
    }
    
    public void setCenter(Center center) {
        this.center = center;
    }
    
}
