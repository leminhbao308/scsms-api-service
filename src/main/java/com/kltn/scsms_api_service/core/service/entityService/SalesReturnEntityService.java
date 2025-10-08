package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.SalesReturn;
import com.kltn.scsms_api_service.core.repository.SalesReturnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesReturnEntityService {
    
    private final SalesReturnRepository repo;
    
    public SalesReturn create(SalesReturn salesReturn) {
        return repo.save(salesReturn);
    }
}
