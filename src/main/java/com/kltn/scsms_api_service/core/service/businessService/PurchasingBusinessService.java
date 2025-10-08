package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.entity.ProductCostStats;
import com.kltn.scsms_api_service.core.entity.PurchaseOrder;
import com.kltn.scsms_api_service.core.entity.PurchaseOrderLine;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PurchaseStatus;
import com.kltn.scsms_api_service.core.entity.enumAttribute.StockRefType;
import com.kltn.scsms_api_service.core.service.entityService.ProductCostStatsService;
import com.kltn.scsms_api_service.core.service.entityService.ProductService;
import com.kltn.scsms_api_service.core.service.entityService.PurchaseOrderEntityService;
import com.kltn.scsms_api_service.core.service.entityService.PurchaseOrderLineEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchasingBusinessService {
    private final PurchaseOrderEntityService purchaseOrderEntityService;
    private final PurchaseOrderLineEntityService polES;
    private final InventoryBusinessService inventoryBS;
    private final ProductCostStatsService costStatsES;
    private final ProductService productES;
    
    
    @Transactional
    public PurchaseOrder createDraft(PurchaseOrder po) {
        po.setStatus(PurchaseStatus.DRAFT);
        return purchaseOrderEntityService.create(po);
    }
    
    
    @Transactional
    public PurchaseOrder submit(PurchaseOrder po) {
        po.setStatus(PurchaseStatus.PENDING_DELIVERY);
        return purchaseOrderEntityService.update(po);
    }
    
    
    // Receive entire PO (simple version; add partial support as needed)
    @Transactional
    public PurchaseOrder receive(UUID poId) {
        PurchaseOrder po = purchaseOrderEntityService.require(poId);
        List<PurchaseOrderLine> lines = polES.byOrder(po.getId());
        for (PurchaseOrderLine line : lines) {
            long remain = line.getQuantityOrdered() - line.getQuantityReceived();
            if (remain <= 0) continue;
            
            // Update inventory & create lot
            inventoryBS.addStock(po.getWarehouse().getId(), line.getProduct().getProductId(), remain, line.getUnitCost(), po.getId(), StockRefType.PURCHASE_ORDER);
            
            // Update peak purchase price
            upsertPeakPrice(line.getProduct().getProductId(), line.getUnitCost());
            
            // Accumulate received qty
            line.setQuantityReceived(line.getQuantityOrdered());
            polES.update(line);
        }
        boolean all = lines.stream().allMatch(l -> Objects.equals(l.getQuantityOrdered(), l.getQuantityReceived()));
        po.setStatus(all ? PurchaseStatus.RECEIVED : PurchaseStatus.PARTIALLY_RECEIVED);
        return purchaseOrderEntityService.update(po);
    }
    
    
    private void upsertPeakPrice(UUID productId, BigDecimal unitCost) {
        ProductCostStats stats = costStatsES.findByProduct(productId)
            .orElseGet(() -> costStatsES.create(ProductCostStats.builder()
                .product(productES.getRefByProductId(productId))
                .peakPurchasePrice(unitCost)
                .build()));
        if (unitCost.compareTo(stats.getPeakPurchasePrice()) > 0) {
            stats.setPeakPurchasePrice(unitCost);
            costStatsES.update(stats);
        }
    }
}
