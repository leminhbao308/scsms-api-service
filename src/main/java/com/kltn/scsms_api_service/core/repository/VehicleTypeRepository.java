package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface VehicleTypeRepository extends JpaRepository<VehicleType, UUID> {
}
