package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.SalesReturn;
import com.kltn.scsms_api_service.core.repository.SalesReturnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesReturnEntityService {
    
    private final SalesReturnRepository repo;
    
    public SalesReturn create(SalesReturn salesReturn) {
        return repo.save(salesReturn);
    }
    
    public List<SalesReturn> getAllReturns() {
        return repo.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<SalesReturn> getSalesReturnsByDateRange(
        LocalDateTime fromDate,
        LocalDateTime toDate) {
        
        log.info("Fetching sales returns from {} to {}", fromDate, toDate);
        List<SalesReturn> returns = repo.findByDateRange(fromDate, toDate);
        log.info("Found {} sales returns", returns.size());
        
        return returns;
    }
}
