package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    
    Optional<Role> findByRoleName(String roleName);
    
    Optional<Role> findByRoleCode(String roleCode);
    
    boolean existsByRoleName(String roleName);
    
    boolean existsByRoleCode(String roleCode);
    
    
    @Query("SELECT r FROM Role r WHERE r.roleName LIKE %:keyword% OR r.description LIKE %:keyword%")
    List<Role> searchByKeyword(@Param("keyword") String keyword);
}
