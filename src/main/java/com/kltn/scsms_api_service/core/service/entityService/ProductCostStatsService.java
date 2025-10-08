package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.ProductCostStats;
import com.kltn.scsms_api_service.core.repository.ProductCostStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCostStatsService {
    
    private final ProductCostStatsRepository repo;
    
    public Optional<ProductCostStats> findByProduct(UUID productId) {
        return repo.findByProductProductId(productId);
    }
    
    public ProductCostStats create(ProductCostStats productCostStats) {
        return repo.save(productCostStats);
    }
    
    public ProductCostStats update(ProductCostStats productCostStats) {
        return repo.save(productCostStats);
    }
}
