package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.PurchaseOrderLine;
import com.kltn.scsms_api_service.core.repository.PurchaseOrderLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderLineEntityService {
    
    private final PurchaseOrderLineRepository repo;
    
    public List<PurchaseOrderLine> byOrder(UUID poId) {
        return repo.findByPurchaseOrderId(poId);
    }
    
    public PurchaseOrderLine update(PurchaseOrderLine line) {
        return repo.save(line);
    }
    
    public PurchaseOrderLine create(PurchaseOrderLine purchaseOrderLine) {
        return repo.save(purchaseOrderLine);
    }
    
    public List<PurchaseOrderLine> getByProductId(UUID productId) {
        return repo.findByProductProductId(productId);
    }
}
