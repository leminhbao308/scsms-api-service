package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.PromotionLine;
import com.kltn.scsms_api_service.core.repository.PromotionLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionLineService {
    
    private final PromotionLineRepository promotionLineRepository;
    
    public PromotionLine save(PromotionLine promotionLine) {
        return promotionLineRepository.save(promotionLine);
    }
    
    public void deleteAllByIds(List<UUID> existingLineIds) {
        promotionLineRepository.deleteAllById(existingLineIds);
    }
}
