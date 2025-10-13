package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.InventoryLevel;
import com.kltn.scsms_api_service.core.repository.InventoryLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryLevelEntityService {
    
    private final InventoryLevelRepository repo;
    
    public Optional<InventoryLevel> find(UUID warehouseId, UUID productId) {
        return repo.findByWarehouseIdAndProductProductId(warehouseId, productId);
    }
    
    public InventoryLevel create(InventoryLevel inventoryLevel) {
        return repo.save(inventoryLevel);
    }
    
    public void update(InventoryLevel inventoryLevel) {
        repo.save(inventoryLevel);
    }
    
    public List<InventoryLevel> findAllByWarehouse(UUID warehouseId) {
        return repo.findAllByWarehouse_Id(warehouseId);
    }
    
    public List<InventoryLevel> getAll() {
        return repo.findAll();
    }
}
