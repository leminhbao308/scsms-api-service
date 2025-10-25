package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.SalesOrder;
import com.kltn.scsms_api_service.core.entity.enumAttribute.SalesStatus;
import com.kltn.scsms_api_service.core.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesOrderEntityService {
    
    private final SalesOrderRepository repo;
    
    public SalesOrder create(SalesOrder salesOrder) {
        return repo.save(salesOrder);
    }
    
    public SalesOrder require(UUID saleOrderId) {
        return repo.findById(saleOrderId).orElseThrow(() -> new IllegalArgumentException("Sales order not found"));
    }
    
    public SalesOrder update(SalesOrder salesOrder) {
        return repo.save(salesOrder);
    }
    
    public List<SalesOrder> getAll() {
        return repo.findByStatusNotOrderByCreatedDateDesc(SalesStatus.RETURNED);
    }
    
    public Page<SalesOrder> getPagedOrders(Pageable pageable) {
        return repo.findByStatusNot(SalesStatus.RETURNED, pageable);
    }
    
    public List<SalesOrder> getAllFullfills() {
        return repo.findByStatusOrderByCreatedDateDesc(SalesStatus.FULFILLED);
    }
}
