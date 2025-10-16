package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.PriceBookItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PriceBookItemRepository extends JpaRepository<PriceBookItem, UUID> {
    Optional<PriceBookItem> findByPriceBookIdAndProductProductId(UUID bookId, UUID productId);
    Optional<PriceBookItem> findByPriceBookIdAndServiceServiceId(UUID bookId, UUID serviceId);
}
