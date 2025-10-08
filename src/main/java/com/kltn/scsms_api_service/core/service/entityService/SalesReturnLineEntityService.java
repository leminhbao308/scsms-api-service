package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.SalesReturnLine;
import com.kltn.scsms_api_service.core.repository.SalesReturnLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesReturnLineEntityService {
    
    private final SalesReturnLineRepository repo;
    
    public SalesReturnLine create(SalesReturnLine salesReturnLine) {
        return repo.save(salesReturnLine);
    }
}
