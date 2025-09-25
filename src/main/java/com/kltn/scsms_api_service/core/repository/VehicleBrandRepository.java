package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.VehicleBrand;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleBrandRepository extends JpaRepository<VehicleBrand, UUID> {
    Optional<VehicleBrand> findByBrandId(UUID brandId);
    
    Optional<VehicleBrand> findByBrandCode(String brandCode);
    
    List<VehicleBrand> findAllByIsActiveAndIsDeleted(Boolean isActive, Boolean isDeleted, Sort sort);
}
