package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.Role;
import com.kltn.scsms_api_service.core.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
