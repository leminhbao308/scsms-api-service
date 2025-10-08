package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.PurchaseOrder;
import com.kltn.scsms_api_service.core.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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
        return repo.findById(purchaseOrderId).orElseThrow(() -> new IllegalArgumentException("Purchase order not found") );
    }
    
    public List<PurchaseOrder> getAll() {
        return repo.findAll();
    }
}
