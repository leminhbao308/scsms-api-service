package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.PriceListDetail;
import com.kltn.scsms_api_service.core.entity.enumAttribute.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PriceListDetailRepository extends JpaRepository<PriceListDetail, UUID> {

    List<PriceListDetail> findByPriceListHeaderPriceListId(UUID priceListId);
    
    void deleteByPriceListHeaderPriceListId(UUID priceListId);
    
    @Query("SELECT d FROM PriceListDetail d WHERE " +
           "d.priceListHeader.priceListId = :priceListId AND " +
           "d.itemType = :itemType AND d.itemId = :itemId")
    Optional<PriceListDetail> findByPriceListAndItem(
            @Param("priceListId") UUID priceListId,
            @Param("itemType") ItemType itemType,
            @Param("itemId") UUID itemId);
    
    @Query("SELECT d FROM PriceListDetail d JOIN d.priceListHeader h " +
           "WHERE h.priceListId = :priceListId AND " +
           "(:itemName IS NULL OR d.itemName LIKE %:itemName%)")
    List<PriceListDetail> findByPriceListIdAndFilters(
            @Param("priceListId") UUID priceListId,
            @Param("itemName") String itemName);
}
