package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.InventoryDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryDetailRepository extends JpaRepository<InventoryDetail, UUID> {

    List<InventoryDetail> findByInventoryInventoryId(UUID inventoryId);
    
    void deleteByInventoryInventoryId(UUID inventoryId);
    
    @Query("SELECT d FROM InventoryDetail d JOIN d.inventory i JOIN d.product p " +
           "WHERE i.inventoryId = :inventoryId AND " +
           "(:productName IS NULL OR p.productName LIKE %:productName%) AND " +
           "(:productSku IS NULL OR p.sku LIKE %:productSku%)")
    List<InventoryDetail> findByInventoryIdAndFilters(
            @Param("inventoryId") UUID inventoryId,
            @Param("productName") String productName,
            @Param("productSku") String productSku);
}
