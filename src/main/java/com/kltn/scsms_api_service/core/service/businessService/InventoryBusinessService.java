package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.entity.*;
import com.kltn.scsms_api_service.core.entity.enumAttribute.StockRefType;
import com.kltn.scsms_api_service.core.entity.enumAttribute.StockTxnType;
import com.kltn.scsms_api_service.core.service.entityService.*;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
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
    private final BranchService branchES;
    private final SalesOrderEntityService salesOrderEntityService;
    
    
    @Transactional
    public void addStock(UUID branchId, UUID productId, Long qty, BigDecimal unitCost, String lotCode, UUID refId, StockRefType refType) {
        increaseStock(branchId, productId, qty, unitCost, lotCode, refId, refType, StockTxnType.PURCHASE_RECEIPT);
    }
    
    
    @Transactional
    public void reserveStock(UUID branchId, UUID productId, Long qty, UUID refId, StockRefType refType) {
        InventoryLevel level = inventoryLevelEntityService.find(branchId, productId).orElseThrow();
        if (level.getAvailable() < qty) {
            Product product = level.getProduct();
            Branch branch = level.getBranch();
            throw new ClientSideException(
                ErrorCode.BAD_REQUEST,
                String.format(
                    "Insufficient available stock for product '%s' (ID: %s) in branch '%s' (ID: %s): requested=%d, available=%d",
                    product != null ? product.getProductName() : "Unknown",
                    product != null ? product.getProductId() : productId,
                    branch != null ? branch.getBranchName() : "Unknown",
                    branch != null ? branch.getBranchId() : branchId,
                    qty,
                    level.getAvailable()
                )
            );
        }
        level.setReserved(level.getReserved() + qty);
        inventoryLevelEntityService.update(level);
        txnES.create(StockTransaction.builder()
            .branch(level.getBranch())
            .product(level.getProduct())
            .type(StockTxnType.RESERVATION).quantity(-qty)
            .refType(refType).refId(refId).build());
    }
    
    
    @Transactional
    public void releaseReservation(UUID branchId, UUID productId, Long qty, UUID refId, StockRefType refType) {
        InventoryLevel level = inventoryLevelEntityService.find(branchId, productId).orElseThrow();
        level.setReserved(Math.max(0, level.getReserved() - qty));
        inventoryLevelEntityService.update(level);
        txnES.create(StockTransaction.builder()
            .branch(level.getBranch())
            .product(level.getProduct())
            .type(StockTxnType.RELEASE).quantity(qty)
            .refType(refType).refId(refId).build());
    }
    
    /**
     * Fulfill stock using First-In-First-Out (FIFO) method
     * Make sure to oldest lots are used first
     *
     * @param branchId  Branch to fulfill from
     * @param productId Product to fulfill
     * @param qty       Quantity to fulfill
     * @param refId     Reference ID for the transaction
     * @param refType   Reference type for the transaction
     */
    @Transactional
    public void fulfillStockFIFO(UUID branchId, UUID productId, Long qty, UUID refId, StockRefType refType) {
        // LỚP BẢO VỆ CUỐI CÙNG: Nếu refType là SALE_ORDER và sales order có originalBookingId → KHÔNG fulfill
        // Đây là lớp bảo vệ để đảm bảo sales order từ booking KHÔNG BAO GIỜ fulfill inventory
        if (refType == StockRefType.SALE_ORDER) {
            try {
                // Kiểm tra xem sales order có lines với originalBookingId không
                SalesOrder salesOrder = salesOrderEntityService.requireWithDetails(refId);
                boolean isFromBooking = salesOrder.getLines().stream()
                    .anyMatch(line -> line.getOriginalBookingId() != null);
                
                if (isFromBooking) {
                    log.error("CRITICAL BLOCK: Attempted to fulfill inventory for sales order {} from booking. " +
                        "This should NEVER happen! Sales order from booking CHỈ thanh toán tiền, KHÔNG động chạm đến tồn kho. " +
                        "BLOCKING inventory fulfillment to prevent duplicate deduction. " +
                        "This is a last-line defense in InventoryBusinessService.fulfillStockFIFO().",
                        refId);
                    throw new ClientSideException(ErrorCode.BAD_REQUEST,
                        "CRITICAL: Cannot fulfill inventory for sales order from booking. " +
                        "Sales order from booking only handles payment, not inventory. " +
                        "Inventory is already handled by booking (reserved at PENDING, fulfilled at IN_PROGRESS).");
                }
            } catch (IllegalArgumentException e) {
                // Sales order not found - có thể refId không phải sales order ID
                // Nhưng vì refType là SALE_ORDER, nên refId PHẢI là sales order ID
                // Nếu không tìm thấy, có thể là lỗi nghiêm trọng - log error nhưng vẫn block để an toàn
                log.error("CRITICAL: refType is SALE_ORDER but sales order {} not found. " +
                    "This may indicate a data inconsistency. BLOCKING fulfillment to prevent inventory errors.",
                    refId);
                throw new ClientSideException(ErrorCode.BAD_REQUEST,
                    "CRITICAL: Sales order not found for refId " + refId + " with refType SALE_ORDER. " +
                    "Cannot fulfill inventory - data inconsistency detected.");
            } catch (ClientSideException e) {
                // Re-throw ClientSideException (đã block)
                throw e;
            } catch (Exception e) {
                // Bất kỳ exception nào khác cũng phải block - không được tiếp tục fulfill
                log.error("CRITICAL ERROR: Exception when checking if sales order {} is from booking: {}. " +
                    "BLOCKING fulfillment to prevent potential duplicate inventory deduction.",
                    refId, e.getMessage(), e);
                throw new ClientSideException(ErrorCode.SYSTEM_ERROR,
                    "CRITICAL: Error checking sales order from booking. Cannot fulfill inventory. " +
                    "Error: " + e.getMessage());
            }
        }
        
        InventoryLevel level = inventoryLevelEntityService.find(branchId, productId).orElseThrow();
        List<InventoryLot> lots = lotES.fifoLots(branchId, productId);
        long remaining = qty;
        for (InventoryLot lot : lots) {
            if (remaining <= 0) break;
            long take = (long) Math.min(remaining, lot.getQuantity());
            if (take <= 0) continue;
            lot.setQuantity(lot.getQuantity() - take);
            lotES.update(lot);
            
            
            txnES.create(StockTransaction.builder()
                .branch(level.getBranch())
                .product(level.getProduct())
                .inventoryLot(lot).type(StockTxnType.SALE)
                .quantity(-take).unitCost(lot.getUnitCost())
                .refType(refType).refId(refId).build());
            remaining -= take;
        }
        if (remaining > 0) throw new ClientSideException(
            ErrorCode.BAD_REQUEST,
            "Not enough stock to fulfill. Branch: " + branchId +
                ", Product: " + productId +
                ", Requested: " + qty +
                ", Unfulfilled: " + remaining
        );
        level.setOnHand(level.getOnHand() - qty);
        level.setReserved(Math.max(0, level.getReserved() - qty));
        inventoryLevelEntityService.update(level);
    }
    
    
    @Transactional
    public void returnToStock(UUID branchId, UUID productId, Long qty, BigDecimal unitCost, UUID refId, StockRefType refType) {
        increaseStock(branchId, productId, qty, unitCost, null, refId, refType, StockTxnType.RETURN);
    }
    
    private void increaseStock(UUID branchId, UUID productId, Long qty, BigDecimal unitCost, String lotCode, UUID refId, StockRefType refType, StockTxnType adjustType) {
        Branch branchRef = branchES.getRefById(branchId);
        Product productRef = productES.getRefByProductId(productId);
        
        InventoryLevel level = inventoryLevelEntityService.find(branchId, productId)
            .orElseGet(() -> inventoryLevelEntityService.create(InventoryLevel.builder()
                .branch(branchRef)
                .product(productRef)
                .onHand(0L).reserved(0L).build()));
        level.setOnHand(level.getOnHand() + qty);
        inventoryLevelEntityService.update(level);
        
        
        InventoryLot lot = InventoryLot.builder()
            .branch(branchRef)
            .product(productRef)
            .receivedAt(LocalDateTime.now())
            .unitCost(unitCost).quantity(qty)
            .lotCode(lotCode)
            .build();
        lotES.create(lot);
        
        
        txnES.create(StockTransaction.builder()
            .branch(branchRef)
            .product(productRef)
            .inventoryLot(lot).type(adjustType)
            .quantity(qty).unitCost(unitCost)
            .refType(refType).refId(refId).build());
    }
}
