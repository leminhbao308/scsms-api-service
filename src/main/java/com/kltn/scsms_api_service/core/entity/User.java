package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.core.constants.GeneralConstant;
import com.kltn.scsms_api_service.core.entity.enumAttribute.CustomerRank;
import com.kltn.scsms_api_service.core.entity.enumAttribute.UserType;
import com.kltn.scsms_api_service.core.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users", schema = GeneralConstant.DB_SCHEMA_DEV)
public class User extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "google_id", unique = true)
    private String googleId;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false, length = Integer.MAX_VALUE)
    private String password;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "address", length = Integer.MAX_VALUE)
    private String address;

    @Column(name = "avatar_url", length = Integer.MAX_VALUE)
    private String avatarUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "role_assigned_at")
    private LocalDateTime roleAssignedAt;

    @CreatedBy
    @Column(name = "role_assigned_by")
    private String roleAssignedBy;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;
    
    // Customer attiributes
    @Enumerated(EnumType.STRING)
    @Column(name = "customer_rank")
    private CustomerRank customerRank;
    
    @Column(name = "accumulated_points")
    @Builder.Default
    private Integer accumulatedPoints = 0;
    
    @Column(name = "total_orders")
    @Builder.Default
    private Integer totalOrders = 0;
    
    @Column(name = "total_spent")
    @Builder.Default
    private Double totalSpent = 0.0;
    
    // Staff attributes
    @Column(name = "hired_at")
    @Builder.Default
    private LocalDateTime hiredAt = LocalDateTime.now();
    
    @Column(name = "citizen_id", unique = true)
    private String citizenId;

    @Override
    protected void onCreate() {
        super.onCreate();

        if (roleAssignedAt == null && role != null) {
            roleAssignedAt = LocalDateTime.now();
        }
    }

    public void setRole(Role role) {
        this.role = role;
        this.roleAssignedAt = LocalDateTime.now();
        this.roleAssignedBy = "SYSTEM";
    }
}
