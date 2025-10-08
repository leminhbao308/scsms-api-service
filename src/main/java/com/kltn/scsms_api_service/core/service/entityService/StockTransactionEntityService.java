package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.StockTransaction;
import com.kltn.scsms_api_service.core.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockTransactionEntityService {
    
    private final StockTransactionRepository repo;
    
    public void create(StockTransaction stockTransaction) {
        repo.save(stockTransaction);
    }
}
