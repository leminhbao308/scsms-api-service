package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    
    @Column(name = "operating_hours")
    @JdbcTypeCode(SqlTypes.JSON)
    private String operatingHours;
    
    @Column(name = "service_capacity")
    @Builder.Default
    private Integer serviceCapacity = 10;
    
    @Column(name = "current_workload")
    @Builder.Default
    private Integer currentWorkload = 0;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "area_sqm")
    private Double areaSqm;
    
    @Column(name = "parking_spaces")
    @Builder.Default
    private Integer parkingSpaces = 0;
    
    @Column(name = "established_date")
    private java.time.LocalDate establishedDate;
    
    @Column(name = "total_employees")
    @Builder.Default
    private Integer totalEmployees = 0;
    
    @Column(name = "total_customers")
    @Builder.Default
    private Integer totalCustomers = 0;
    
    @Column(name = "monthly_revenue")
    @Builder.Default
    private Double monthlyRevenue = 0.0;
    
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
    
    // Contact information as JSON
    @Column(name = "contact_info")
    @JdbcTypeCode(SqlTypes.JSON)
    private String contactInfo;
    
    // Facilities as JSON array
    @Column(name = "facilities")
    @JdbcTypeCode(SqlTypes.JSON)
    private String facilities;
    
    // Services offered as JSON array
    @Column(name = "services_offered")
    @JdbcTypeCode(SqlTypes.JSON)
    private String servicesOffered;
    
    // Operating status
    @Column(name = "operating_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OperatingStatus operatingStatus = OperatingStatus.ACTIVE;
    
    public enum OperatingStatus {
        ACTIVE, INACTIVE, SUSPENDED, MAINTENANCE, CLOSED
    }
    
    // Branch type
    @Column(name = "branch_type")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BranchType branchType = BranchType.STANDARD;
    
    public enum BranchType {
        HEADQUARTERS, STANDARD, EXPRESS, PREMIUM, MOBILE
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
    
    public boolean isAtCapacity() {
        return currentWorkload >= serviceCapacity;
    }
    
    public double getUtilizationRate() {
        if (serviceCapacity == 0) return 0.0;
        return (double) currentWorkload / serviceCapacity * 100;
    }
}
