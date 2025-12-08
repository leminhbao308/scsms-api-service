package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.StockTransaction;
import com.kltn.scsms_api_service.core.entity.enumAttribute.StockTxnType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, UUID> {

  /**
   * Find all transactions for a specific inventory lot
   */
  List<StockTransaction> findByInventoryLot_IdOrderByCreatedDateAsc(UUID lotId);

  /**
   * Find all transactions for a product in a branch
   */
  List<StockTransaction> findByBranch_BranchIdAndProduct_ProductIdOrderByCreatedDateAsc(UUID branchId, UUID productId);

  /**
   * Sum quantity by lot and transaction type
   */
  @Query("SELECT COALESCE(SUM(st.quantity), 0) FROM StockTransaction st " +
      "WHERE st.inventoryLot.id = :lotId AND st.type = :txnType")
  Long sumQuantityByLotAndType(@Param("lotId") UUID lotId, @Param("txnType") StockTxnType txnType);

  /**
   * Sum all quantity changes for a lot (to get current quantity)
   */
  @Query("SELECT COALESCE(SUM(st.quantity), 0) FROM StockTransaction st " +
      "WHERE st.inventoryLot.id = :lotId")
  Long sumQuantityByLot(@Param("lotId") UUID lotId);

  /**
   * Find transactions by reference ID and reference type
   */
  List<StockTransaction> findByRefIdAndRefTypeOrderByCreatedDateAsc(UUID refId, com.kltn.scsms_api_service.core.entity.enumAttribute.StockRefType refType);

  /**
   * Find transactions by reference ID, reference type, and transaction type
   */
  @Query("SELECT st FROM StockTransaction st " +
      "WHERE st.refId = :refId AND st.refType = :refType AND st.type = :txnType " +
      "ORDER BY st.createdDate ASC")
  List<StockTransaction> findByRefIdAndRefTypeAndType(
      @Param("refId") UUID refId,
      @Param("refType") com.kltn.scsms_api_service.core.entity.enumAttribute.StockRefType refType,
      @Param("txnType") StockTxnType txnType);

  /**
   * Sum reserved quantity for a booking/product
   */
  @Query("SELECT COALESCE(SUM(ABS(st.quantity)), 0) FROM StockTransaction st " +
      "WHERE st.refId = :refId AND st.refType = :refType AND st.type = :txnType " +
      "AND st.product.productId = :productId AND st.branch.branchId = :branchId")
  Long sumReservedQuantity(
      @Param("refId") UUID refId,
      @Param("refType") com.kltn.scsms_api_service.core.entity.enumAttribute.StockRefType refType,
      @Param("txnType") StockTxnType txnType,
      @Param("productId") UUID productId,
      @Param("branchId") UUID branchId);
}
