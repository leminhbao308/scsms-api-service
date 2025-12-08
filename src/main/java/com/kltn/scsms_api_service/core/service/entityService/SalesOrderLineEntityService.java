package com.kltn.scsms_api_service.core.service.entityService;

import com.kltn.scsms_api_service.core.entity.SalesOrderLine;
import com.kltn.scsms_api_service.core.repository.SalesOrderLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesOrderLineEntityService {
    
    private final SalesOrderLineRepository repo;
    
    public List<SalesOrderLine> byOrder(UUID soId) {
        return repo.findBySalesOrderId(soId);
    }
    
    /**
     * Tìm tất cả sales order lines có originalBookingId
     */
    public List<SalesOrderLine> findByOriginalBookingId(UUID bookingId) {
        return repo.findByOriginalBookingId(bookingId);
    }
    
    public SalesOrderLine update(SalesOrderLine line) {
        return repo.save(line);
    }
    
    public SalesOrderLine create(SalesOrderLine salesOrderLine) {
        return repo.save(salesOrderLine);
    }
}
