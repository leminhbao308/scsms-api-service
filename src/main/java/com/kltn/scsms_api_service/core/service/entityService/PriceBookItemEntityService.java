package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.PriceBookItem;
import com.kltn.scsms_api_service.core.repository.PriceBookItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceBookItemEntityService {
    private final PriceBookItemRepository repo;
    
    public Optional<PriceBookItem> findByPriceBookIdAndProductId(UUID id, UUID productId) {
        return repo.findByPriceBookIdAndProductProductId(id, productId);
    }
    
    public PriceBookItem create(PriceBookItem item) {
        return repo.save(item);
    }
}
