package com.kltn.scsms_api_service.core.repository;

import com.kltn.scsms_api_service.core.entity.ProductCostStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductCostStatsRepository extends JpaRepository<ProductCostStats, UUID> {
    Optional<ProductCostStats> findByProductProductId(UUID productProductId);
}
