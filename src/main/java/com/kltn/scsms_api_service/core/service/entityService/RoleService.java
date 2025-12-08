package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.dto.response.RoleResponse;
import com.kltn.scsms_api_service.core.entity.Role;
import com.kltn.scsms_api_service.core.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {
    
    private final RoleRepository roleRepository;
    
    public boolean isRoleExistedByRoleCode(String roleCode) {
        return roleRepository.existsByRoleCode(roleCode);
    }
    
    public Optional<Role> getRoleByRoleCode(String roleCode) {
        return roleRepository.findByRoleCode(roleCode);
    }
    
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
            .map(role -> RoleResponse.builder()
                .roleId(role.getRoleId())
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .build())
            .toList();
    }
}
