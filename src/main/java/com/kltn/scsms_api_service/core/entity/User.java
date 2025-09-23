package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.core.abstracts.AuditEntity;
import com.kltn.scsms_api_service.core.constants.GeneralConstant;
import com.kltn.scsms_api_service.core.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
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
    private Boolean isActive = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "role_assigned_at")
    private LocalDateTime roleAssignedAt;

    @Column(name = "role_assigned_by")
    private String roleAssignedBy;

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
    }
}