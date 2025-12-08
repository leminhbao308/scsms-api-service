package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.SupplierProduct;
import com.kltn.scsms_api_service.core.repository.SupplierProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierProductEntityService {
    
    private final SupplierProductRepository repo;
    
    public Page<SupplierProduct> findAll(PageRequest pr) {
        return repo.findAll(pr);
    }
}
