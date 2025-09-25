package com.kltn.scsms_api_service.core.entity;

import com.kltn.scsms_api_service.abstracts.AuditEntity;
import com.kltn.scsms_api_service.core.constants.GeneralConstant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "permissions", schema = GeneralConstant.DB_SCHEMA_DEV)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Permission extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "permission_id", nullable = false)
    private UUID permissionId;
    
    @Column(name = "permission_name", unique = true, nullable = false)
    private String permissionName;

    @Column(name = "permission_code", unique = true, nullable = false)
    private String permissionCode;
    
    @Column(name = "module", nullable = false)
    private String module;
    
    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;
    
    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<RolePermission> rolePermissions = new HashSet<>();
}
