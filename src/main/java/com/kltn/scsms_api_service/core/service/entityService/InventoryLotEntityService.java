package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.InventoryLot;
import com.kltn.scsms_api_service.core.repository.InventoryLotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryLotEntityService {
    
    private final InventoryLotRepository repo;
    
    /**
     * Get all inventory lots in a warehouse for a specific product, ordered by received date (FIFO).
     *
     * @param warehouseId ID of the warehouse
     * @param productId   ID of the product
     * @return List of InventoryLot objects ordered by received date
     */
    public List<InventoryLot> fifoLots(UUID warehouseId, UUID productId) {
        return repo.findByWarehouseIdAndProductProductIdOrderByReceivedAtAsc(warehouseId, productId);
    }
    
    public InventoryLot create(InventoryLot lot) {
        return repo.save(lot);
    }
    
    public InventoryLot update(InventoryLot lot) {
        return repo.save(lot);
    }
    
    public List<InventoryLot> findByWarehouse(UUID warehouseId) {
        return repo.findAllByWarehouse_Id(warehouseId);
    }
    
    public List<InventoryLot> getAll() {
        return repo.findAll();
    }
}
