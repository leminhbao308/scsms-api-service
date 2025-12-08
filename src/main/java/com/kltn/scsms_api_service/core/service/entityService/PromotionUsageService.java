package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.PromotionUsage;
import com.kltn.scsms_api_service.core.repository.PromotionUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromotionUsageService {
    
    private final PromotionUsageRepository promotionUsageRepository;
    
    public Page<PromotionUsage> findAll(Specification<PromotionUsage> spec, Pageable pageable) {
        return promotionUsageRepository.findAll(spec, pageable);
    }
}
