package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.PurchaseOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchaseOrderLineRepository extends JpaRepository<PurchaseOrderLine, UUID> {
    List<PurchaseOrderLine> findByPurchaseOrderId(UUID poId);
    
    List<PurchaseOrderLine> findByProductProductId(UUID productProductId);
}
