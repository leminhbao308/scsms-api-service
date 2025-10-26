package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.PriceBookItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PriceBookItemRepository extends JpaRepository<PriceBookItem, UUID> {
    Optional<PriceBookItem> findByPriceBookIdAndProductProductId(UUID bookId, UUID productId);

    Optional<PriceBookItem> findByPriceBookIdAndServiceServiceId(UUID bookId, UUID serviceId);

    /**
     * Batch fetch service prices from price book
     * Prevents N+1 query pattern on frontend
     * Returns items with service details for the given service IDs
     */
    @Query("SELECT pbi FROM PriceBookItem pbi " +
            "LEFT JOIN FETCH pbi.service s " +
            "WHERE pbi.priceBook.id = :priceBookId " +
            "AND pbi.service IS NOT NULL " +
            "AND pbi.service.serviceId IN :serviceIds")
    List<PriceBookItem> findServicePricesBatch(
            @Param("priceBookId") UUID priceBookId,
            @Param("serviceIds") List<UUID> serviceIds);
}
