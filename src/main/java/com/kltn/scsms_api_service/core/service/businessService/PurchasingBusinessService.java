package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.request.CreatePOLine;
import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.request.CreatePORequest;
import com.kltn.scsms_api_service.core.entity.ProductCostStats;
import com.kltn.scsms_api_service.core.entity.PurchaseOrder;
import com.kltn.scsms_api_service.core.entity.PurchaseOrderLine;
import com.kltn.scsms_api_service.core.entity.enumAttribute.StockRefType;
import com.kltn.scsms_api_service.core.service.entityService.*;
import com.kltn.scsms_api_service.mapper.PurchaseOrderLineMapper;
import com.kltn.scsms_api_service.mapper.PurchaseOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
    private final SupplierService supplierES;
    private final BranchService branchES;
    
    private final PurchaseOrderMapper poMapper;
    private final PurchaseOrderLineMapper polMapper;
    
    @Transactional
    public PurchaseOrder createDraft(CreatePORequest poReq) {
        PurchaseOrder createdPO = purchaseOrderEntityService.create(
            poMapper.toEntity(poReq, branchES)
        );
        
        // Create and process lines
        for (CreatePOLine lineReq : poReq.getLines()) {
            if (lineReq.getQty() <= 0) continue;
            
            // Create line with proper associations
            PurchaseOrderLine newPol = polMapper.toEntity(lineReq, productES, supplierES);
            newPol.setPurchaseOrder(createdPO);
            polES.create(newPol);
            
            // Update inventory & create lot
            inventoryBS.addStock(
                createdPO.getBranch().getBranchId(),
                lineReq.getProductId(),
                lineReq.getQty(),
                lineReq.getUnitCost(),
                lineReq.getLotCode(),
                createdPO.getId(),
                StockRefType.PURCHASE_ORDER
            );
            
            // Update peak purchase price
            upsertPeakPrice(lineReq.getProductId(), lineReq.getUnitCost());
        }
        
        return createdPO;
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
    
    public List<PurchaseOrderLine> getProductPOHistory(UUID productId) {
        return polES.getByProductId(productId);
    }
    
    public List<PurchaseOrder> getPurchaseOrdersByDateAndBranch(LocalDateTime fromDate, LocalDateTime toDate, UUID branchId) {
        return purchaseOrderEntityService.getByDateAndBranch(fromDate, toDate, branchId);
    }
}
