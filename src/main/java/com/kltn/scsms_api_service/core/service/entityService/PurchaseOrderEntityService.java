package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.PurchaseOrder;
import com.kltn.scsms_api_service.core.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderEntityService {
    
    private final PurchaseOrderRepository repo;
    
    public PurchaseOrder create(PurchaseOrder purchaseOrder) {
        return repo.save(purchaseOrder);
    }
    
    public PurchaseOrder update(PurchaseOrder purchaseOrder) {
        return repo.save(purchaseOrder);
    }
    
    public PurchaseOrder require(UUID purchaseOrderId) {
        return repo.findById(purchaseOrderId).orElseThrow(() -> new IllegalArgumentException("Purchase order not found"));
    }
    
    public List<PurchaseOrder> getAll() {
        return repo.findAll();
    }
    
    public List<PurchaseOrder> getByDateAndBranch(LocalDateTime fromDate, LocalDateTime toDate, UUID branchId) {
        if (fromDate == null && toDate == null && branchId == null) {
            return repo.findAll();
        } else if (fromDate == null && toDate == null) {
            return repo.findByBranch_BranchId(branchId);
        } else if (fromDate == null) {
            return repo.findByCreatedDateLessThanEqualAndBranch_BranchId(toDate, branchId);
        } else if (toDate == null) {
            return repo.findByCreatedDateGreaterThanEqualAndBranch_BranchId(fromDate, branchId);
        } else if (branchId == null) {
            return repo.findByCreatedDateBetween(fromDate, toDate);
        }
        return repo.findByCreatedDateBetweenAndBranch_BranchId(fromDate, toDate, branchId);
    }
}
