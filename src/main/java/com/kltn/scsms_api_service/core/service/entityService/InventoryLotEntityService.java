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
     * Get all inventory lots in a branch for a specific product, ordered by
     * received date (FIFO).
     *
     * @param branchId  ID of the branch
     * @param productId ID of the product
     * @return List of InventoryLot objects ordered by received date
     */
    public List<InventoryLot> fifoLots(UUID branchId, UUID productId) {
        return repo.findByBranch_BranchIdAndProduct_ProductIdOrderByReceivedAtAsc(branchId, productId);
    }

    public InventoryLot create(InventoryLot lot) {
        return repo.save(lot);
    }

    public InventoryLot update(InventoryLot lot) {
        return repo.save(lot);
    }

    public List<InventoryLot> findByBranch(UUID branchId) {
        return repo.findAllByBranch_BranchId(branchId);
    }

    public List<InventoryLot> findByProduct(UUID productId) {
        return repo.findAllByProduct_ProductId(productId);
    }

    public List<InventoryLot> findByProductAndBranch(UUID productId, UUID branchId) {
        return repo.findAllByProduct_ProductIdAndBranch_BranchId(productId, branchId);
    }

    public List<InventoryLot> getAll() {
        return repo.findAll();
    }
}
