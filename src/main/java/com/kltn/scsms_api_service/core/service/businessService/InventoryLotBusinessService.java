package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.InventoryLotDTO;
import com.kltn.scsms_api_service.core.entity.InventoryLot;
import com.kltn.scsms_api_service.core.entity.StockTransaction;
import com.kltn.scsms_api_service.core.repository.InventoryLotRepository;
import com.kltn.scsms_api_service.core.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryLotBusinessService {

  private final InventoryLotRepository inventoryLotRepo;
  private final StockTransactionRepository stockTxnRepo;

  /**
   * Get all inventory lots for a product with calculated quantities
   */
  public List<InventoryLotDTO> getProductLots(UUID branchId, UUID productId) {
    List<InventoryLot> lots = inventoryLotRepo
        .findByBranch_BranchIdAndProduct_ProductIdOrderByReceivedAtAsc(branchId, productId);

    return lots.stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  /**
   * Get all inventory lots for a branch with calculated quantities
   */
  public List<InventoryLotDTO> getBranchLots(UUID branchId) {
    List<InventoryLot> lots = inventoryLotRepo.findAllByBranch_BranchId(branchId);

    return lots.stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  /**
   * Convert InventoryLot entity to DTO with calculated quantities
   */
  private InventoryLotDTO convertToDTO(InventoryLot lot) {
    // Get all transactions for this lot
    List<StockTransaction> transactions = stockTxnRepo
        .findByInventoryLot_IdOrderByCreatedDateAsc(lot.getId());

    // Calculate quantities
    long qtyReceived = 0L;
    long qtySold = 0L;
    long qtyReserved = 0L;
    long qtyReturned = 0L;
    long qtyAdjustment = 0L;

    for (StockTransaction txn : transactions) {
      switch (txn.getType()) {
        case PURCHASE_RECEIPT:
          qtyReceived += txn.getQuantity();
          break;
        case SALE:
          qtySold += Math.abs(txn.getQuantity()); // Sales are negative
          break;
        case RESERVATION:
          qtyReserved += Math.abs(txn.getQuantity()); // Reservations are negative
          break;
        case RELEASE:
          qtyReserved -= txn.getQuantity(); // Release adds back to reserved
          break;
        case RETURN:
          qtyReturned += txn.getQuantity();
          break;
        case ADJUSTMENT:
          qtyAdjustment += txn.getQuantity();
          break;
      }
    }

    // Current quantity = received + returned + adjustment - sold - reserved
    long qtyCurrent = qtyReceived + qtyReturned + qtyAdjustment - qtySold;
    long qtyAvailable = qtyCurrent - qtyReserved;

    // Determine status
    String status = determineStatus(lot, qtyCurrent);

    return InventoryLotDTO.builder()
        .lotId(lot.getId())
        .lotCode(lot.getLotCode())
        .productId(lot.getProduct().getProductId())
        .productName(lot.getProduct().getProductName())
        .productSku(lot.getProduct().getSku())
        .supplierId(lot.getSupplier() != null ? lot.getSupplier().getSupplierId() : null)
        .supplierName(lot.getSupplier() != null ? lot.getSupplier().getSupplierName() : null)
        .branchId(lot.getBranch().getBranchId())
        .branchName(lot.getBranch().getBranchName())
        .receivedAt(lot.getReceivedAt())
        .expiryDate(lot.getExpiryDate())
        .unitCost(lot.getUnitCost())
        .qtyReceived(qtyReceived)
        .qtyCurrent(qtyCurrent)
        .qtySold(qtySold)
        .qtyReserved(qtyReserved)
        .qtyAvailable(qtyAvailable)
        .status(status)
        .createdDate(lot.getCreatedDate())
        .createdBy(lot.getCreatedBy())
        .build();
  }

  /**
   * Determine lot status based on quantity and expiry date
   */
  private String determineStatus(InventoryLot lot, long qtyCurrent) {
    // Check if expired
    if (lot.getExpiryDate() != null && lot.getExpiryDate().isBefore(LocalDateTime.now())) {
      return "EXPIRED";
    }

    // Check if depleted
    if (qtyCurrent <= 0) {
      return "DEPLETED";
    }

    // Check if expiring soon (within 30 days)
    if (lot.getExpiryDate() != null &&
        lot.getExpiryDate().isBefore(LocalDateTime.now().plusDays(30))) {
      return "EXPIRING_SOON";
    }

    return "ACTIVE";
  }

  /**
   * Get inventory lot summary statistics for a product
   */
  public Map<String, Object> getProductLotSummary(UUID branchId, UUID productId) {
    List<InventoryLotDTO> lots = getProductLots(branchId, productId);

    long totalLots = lots.size();
    long activeLots = lots.stream().filter(l -> "ACTIVE".equals(l.getStatus())).count();
    long expiringSoonLots = lots.stream().filter(l -> "EXPIRING_SOON".equals(l.getStatus())).count();
    long expiredLots = lots.stream().filter(l -> "EXPIRED".equals(l.getStatus())).count();
    long depletedLots = lots.stream().filter(l -> "DEPLETED".equals(l.getStatus())).count();

    long totalQtyReceived = lots.stream().mapToLong(InventoryLotDTO::getQtyReceived).sum();
    long totalQtyCurrent = lots.stream().mapToLong(InventoryLotDTO::getQtyCurrent).sum();
    long totalQtySold = lots.stream().mapToLong(InventoryLotDTO::getQtySold).sum();
    long totalQtyReserved = lots.stream().mapToLong(InventoryLotDTO::getQtyReserved).sum();
    long totalQtyAvailable = lots.stream().mapToLong(InventoryLotDTO::getQtyAvailable).sum();

    Map<String, Object> summary = new HashMap<>();
    summary.put("total_lots", totalLots);
    summary.put("active_lots", activeLots);
    summary.put("expiring_soon_lots", expiringSoonLots);
    summary.put("expired_lots", expiredLots);
    summary.put("depleted_lots", depletedLots);
    summary.put("total_qty_received", totalQtyReceived);
    summary.put("total_qty_current", totalQtyCurrent);
    summary.put("total_qty_sold", totalQtySold);
    summary.put("total_qty_reserved", totalQtyReserved);
    summary.put("total_qty_available", totalQtyAvailable);
    summary.put("lots", lots);

    return summary;
  }
}
