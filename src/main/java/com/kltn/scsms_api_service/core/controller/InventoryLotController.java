package com.kltn.scsms_api_service.core.controller;

import com.kltn.scsms_api_service.core.dto.InventoryLotDTO;
import com.kltn.scsms_api_service.core.dto.StockTransactionDTO;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.service.businessService.InventoryLotBusinessService;
import com.kltn.scsms_api_service.core.service.businessService.StockTransactionBusinessService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/inventory/lots")
@RequiredArgsConstructor
public class InventoryLotController {

  private final InventoryLotBusinessService inventoryLotBS;
  private final StockTransactionBusinessService stockTxnBS;

  /**
   * Get all inventory lots for a specific product in a branch
   *
   * @param branchId  Branch ID
   * @param productId Product ID
   * @return List of inventory lots with quantities
   */
  @GetMapping("/product")
  public ResponseEntity<ApiResponse<List<InventoryLotDTO>>> getProductLots(
      @RequestParam("branchId") UUID branchId,
      @RequestParam("productId") UUID productId) {

    List<InventoryLotDTO> lots = inventoryLotBS.getProductLots(branchId, productId);
    return ResponseBuilder.success("Get product lots successfully", lots);
  }

  /**
   * Get all inventory lots for a branch
   *
   * @param branchId Branch ID
   * @return List of all inventory lots in the branch
   */
  @GetMapping("/branch/{branchId}")
  public ResponseEntity<ApiResponse<List<InventoryLotDTO>>> getBranchLots(@PathVariable UUID branchId) {
    List<InventoryLotDTO> lots = inventoryLotBS.getBranchLots(branchId);
    return ResponseBuilder.success("Get branch lots successfully", lots);
  }

  /**
   * Get inventory lot summary statistics for a product
   * Includes total lots, active/expired/depleted counts, and quantity summaries
   *
   * @param branchId  Branch ID
   * @param productId Product ID
   * @return Summary statistics and list of lots
   */
  @GetMapping("/product/summary")
  public ResponseEntity<ApiResponse<Map<String, Object>>> getProductLotSummary(
      @RequestParam("branchId") UUID branchId,
      @RequestParam("productId") UUID productId) {

    Map<String, Object> summary = inventoryLotBS.getProductLotSummary(branchId, productId);
    return ResponseBuilder.success("Get product lot summary successfully", summary);
  }

  /**
   * Get stock transaction history for a product in a branch
   * Returns all stock transactions (inbound/outbound) with details
   *
   * @param branchId  Branch ID
   * @param productId Product ID
   * @return List of stock transactions
   */
  @GetMapping("/product/transactions")
  public ResponseEntity<ApiResponse<List<StockTransactionDTO>>> getProductTransactionHistory(
      @RequestParam("branchId") UUID branchId,
      @RequestParam("productId") UUID productId) {

    List<StockTransactionDTO> transactions = stockTxnBS.getProductTransactionHistory(branchId, productId);
    return ResponseBuilder.success("Get transaction history successfully", transactions);
  }
}
