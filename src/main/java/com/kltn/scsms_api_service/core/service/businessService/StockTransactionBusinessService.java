package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.dto.StockTransactionDTO;
import com.kltn.scsms_api_service.core.entity.StockTransaction;
import com.kltn.scsms_api_service.core.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockTransactionBusinessService {

  private final StockTransactionRepository stockTxnRepo;

  /**
   * Get all stock transactions for a product in a branch
   */
  public List<StockTransactionDTO> getProductTransactionHistory(UUID branchId, UUID productId) {
    List<StockTransaction> transactions = stockTxnRepo
        .findByBranch_BranchIdAndProduct_ProductIdOrderByCreatedDateAsc(branchId, productId);

    return transactions.stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  /**
   * Convert StockTransaction entity to DTO
   */
  private StockTransactionDTO convertToDTO(StockTransaction txn) {
    return StockTransactionDTO.builder()
        .id(txn.getId())
        .type(txn.getType().name())
        .quantity(txn.getQuantity())
        .unitCost(txn.getUnitCost())
        .lotCode(txn.getInventoryLot() != null ? txn.getInventoryLot().getLotCode() : null)
        .lotId(txn.getInventoryLot() != null ? txn.getInventoryLot().getId() : null)
        .refType(txn.getRefType() != null ? txn.getRefType().name() : null)
        .refId(txn.getRefId())
        .branchId(txn.getBranch().getBranchId())
        .branchName(txn.getBranch().getBranchName())
        .productId(txn.getProduct().getProductId())
        .productName(txn.getProduct().getProductName())
        .productSku(txn.getProduct().getSku())
        .supplierName(
            txn.getInventoryLot() != null && txn.getInventoryLot().getSupplier() != null
                ? txn.getInventoryLot().getSupplier().getSupplierName()
                : null)
        .createdDate(txn.getCreatedDate())
        .createdBy(txn.getCreatedBy())
        .modifiedDate(txn.getModifiedDate())
        .modifiedBy(txn.getModifiedBy())
        .build();
  }
}
