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
    
    public Optional<PriceBookItem> findByPriceBookIdAndServiceId(UUID id, UUID serviceId) {
        return repo.findByPriceBookIdAndServiceServiceId(id, serviceId);
    }
    
    public Optional<PriceBookItem> findByPriceBookIdAndServicePackageId(UUID id, UUID packageId) {
        return repo.findByPriceBookIdAndServicePackagePackageId(id, packageId);
    }
    
    public PriceBookItem create(PriceBookItem item) {
        return repo.save(item);
    }
    
    public PriceBookItem update(PriceBookItem item) {
        return repo.save(item);
    }
    
    public void delete(UUID id) {
        repo.deleteById(id);
    }
    
    public Optional<PriceBookItem> findById(UUID id) {
        return repo.findById(id);
    }
}
