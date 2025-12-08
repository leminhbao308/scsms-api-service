package com.kltn.scsms_api_service.core.entity.compositId;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionId implements Serializable {
    private UUID role;
    private UUID permission;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RolePermissionId that)) return false;
        return Objects.equals(role, that.role) &&
            Objects.equals(permission, that.permission);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(role, permission);
    }
}
