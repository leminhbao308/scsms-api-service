package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    Optional<Warehouse> findByBranchBranchId(UUID branchId);
}
