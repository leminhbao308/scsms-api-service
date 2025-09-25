package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.VehicleBrand;
import com.kltn.scsms_api_service.core.entity.VehicleType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleTypeRepository extends JpaRepository<VehicleType, UUID> {
    Optional<VehicleType> findByTypeId(UUID typeId);
    
    Optional<VehicleType> findByTypeCode(String typeCode);
    
    List<VehicleType> findAllByIsActiveAndIsDeleted(Boolean isActive, Boolean isDeleted, Sort sort);
}
