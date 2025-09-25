package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.VehicleModel;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleModelRepository extends JpaRepository<VehicleModel, UUID> {
    List<VehicleModel> findAllByIsActiveAndIsDeleted(boolean isActive, boolean isDeleted, Sort sort);
    
    Optional<VehicleModel> findByModelId(UUID modelId);
    
    Optional<VehicleModel> findByModelCode(String modelCode);
}
