package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.entity.*;
import com.kltn.scsms_api_service.core.entity.enumAttribute.StockRefType;
import com.kltn.scsms_api_service.core.entity.enumAttribute.StockTxnType;
import com.kltn.scsms_api_service.core.service.entityService.*;
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
public class InventoryBusinessService {
    private final InventoryLevelEntityService inventoryLevelEntityService;
    private final InventoryLotEntityService lotES;
    private final StockTransactionEntityService txnES;
    private final ProductService productES;
    private final WarehouseEntityService warehouseES;
    
    
    @Transactional
    public void addStock(UUID warehouseId, UUID productId, Long qty, BigDecimal unitCost, String lotCode, UUID refId, StockRefType refType) {
        increaseStock(warehouseId, productId, qty, unitCost, lotCode, refId, refType, StockTxnType.PURCHASE_RECEIPT);
    }
    
    
    @Transactional
    public void reserveStock(UUID warehouseId, UUID productId, Long qty, UUID refId, StockRefType refType) {
        InventoryLevel level = inventoryLevelEntityService.find(warehouseId, productId).orElseThrow();
        if (level.getAvailable() < qty) {
            Product product = level.getProduct();
            Warehouse warehouse = level.getWarehouse();
            throw new IllegalStateException(
                String.format(
                    "Insufficient available stock for product '%s' (ID: %s) in warehouse '%s' (ID: %s): requested=%d, available=%d",
                    product != null ? product.getProductName() : "Unknown",
                    product != null ? product.getProductId() : productId,
                    warehouse != null ? warehouse.getId() : warehouseId,
                    qty,
                    level.getAvailable()
                )
            );
        }
        level.setReserved(level.getReserved() + qty);
        inventoryLevelEntityService.update(level);
        txnES.create(StockTransaction.builder()
            .warehouse(level.getWarehouse()).product(level.getProduct())
            .type(StockTxnType.RESERVATION).quantity(-qty)
            .refType(refType).refId(refId).build());
    }
    
    
    @Transactional
    public void releaseReservation(UUID warehouseId, UUID productId, Long qty, UUID refId, StockRefType refType) {
        InventoryLevel level = inventoryLevelEntityService.find(warehouseId, productId).orElseThrow();
        level.setReserved(Math.max(0, level.getReserved() - qty));
        inventoryLevelEntityService.update(level);
        txnES.create(StockTransaction.builder()
            .warehouse(level.getWarehouse()).product(level.getProduct())
            .type(StockTxnType.RELEASE).quantity(qty)
            .refType(refType).refId(refId).build());
    }
    
    /**
     * Fulfill stock using First-In-First-Out (FIFO) method
     * Make sure to oldest lots are used first
     *
     * @param warehouseId Warehouse to fulfill from
     * @param productId   Product to fulfill
     * @param qty         Quantity to fulfill
     * @param refId       Reference ID for the transaction
     * @param refType     Reference type for the transaction
     */
    @Transactional
    public void fulfillStockFIFO(UUID warehouseId, UUID productId, Long qty, UUID refId, StockRefType refType) {
        InventoryLevel level = inventoryLevelEntityService.find(warehouseId, productId).orElseThrow();
        List<InventoryLot> lots = lotES.fifoLots(warehouseId, productId);
        long remaining = qty;
        for (InventoryLot lot : lots) {
            if (remaining <= 0) break;
            long take = (long) Math.min(remaining, lot.getQuantity());
            if (take <= 0) continue;
            lot.setQuantity(lot.getQuantity() - take);
            lotES.update(lot);
            
            
            txnES.create(StockTransaction.builder()
                .warehouse(level.getWarehouse()).product(level.getProduct())
                .inventoryLot(lot).type(StockTxnType.SALE)
                .quantity(-take).unitCost(lot.getUnitCost())
                .refType(refType).refId(refId).build());
            remaining -= take;
        }
        if (remaining > 0) throw new IllegalStateException(
            "Not enough stock to fulfill. Warehouse: " + warehouseId +
                ", Product: " + productId +
                ", Requested: " + qty +
                ", Unfulfilled: " + remaining
        );
        level.setOnHand(level.getOnHand() - qty);
        level.setReserved(Math.max(0, level.getReserved() - qty));
        inventoryLevelEntityService.update(level);
    }
    
    
    @Transactional
    public void returnToStock(UUID warehouseId, UUID productId, Long qty, BigDecimal unitCost, UUID refId, StockRefType refType) {
        increaseStock(warehouseId, productId, qty, unitCost, null, refId, refType, StockTxnType.RETURN);
    }
    
    private void increaseStock(UUID warehouseId, UUID productId, Long qty, BigDecimal unitCost, String lotCode, UUID refId, StockRefType refType, StockTxnType adjustType) {
        Warehouse warehouseRef = warehouseES.getRefByWarehouseId(warehouseId);
        Product productRef = productES.getRefByProductId(productId);
        
        InventoryLevel level = inventoryLevelEntityService.find(warehouseId, productId)
            .orElseGet(() -> inventoryLevelEntityService.create(InventoryLevel.builder()
                .warehouse(warehouseRef)
                .product(productRef)
                .onHand(0L).reserved(0L).build()));
        level.setOnHand(level.getOnHand() + qty);
        inventoryLevelEntityService.update(level);
        
        
        InventoryLot lot = InventoryLot.builder()
            .warehouse(warehouseRef)
            .product(productRef)
            .receivedAt(LocalDateTime.now())
            .unitCost(unitCost).quantity(qty)
            .lotCode(lotCode)
            .build();
        lotES.create(lot);
        
        
        txnES.create(StockTransaction.builder()
            .warehouse(warehouseRef).product(productRef)
            .inventoryLot(lot).type(adjustType)
            .quantity(qty).unitCost(unitCost)
            .refType(refType).refId(refId).build());
    }
}
