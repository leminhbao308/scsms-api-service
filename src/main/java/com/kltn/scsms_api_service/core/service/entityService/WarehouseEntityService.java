package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.Warehouse;
import com.kltn.scsms_api_service.core.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseEntityService {
    
    private final WarehouseRepository repo;
    
    public Optional<Warehouse> findByBranch(UUID branchId) {
        return repo.findByBranchBranchId(branchId);
    }
    
    public Warehouse saveWarehouse(Warehouse newWarehouse) {
        return repo.save(newWarehouse);
    }
    
    public Warehouse getRefByWarehouseId(UUID warehouseId) {
        return repo.getReferenceById(warehouseId);
    }
    
    public Optional<Warehouse> find(UUID warehouseId) {
        return repo.findById(warehouseId);
    }
    
    public List<Warehouse> findAll() {
        return repo.findAll();
    }
}
