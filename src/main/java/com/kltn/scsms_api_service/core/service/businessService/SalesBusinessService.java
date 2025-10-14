package com.kltn.scsms_api_service.core.service.businessService;

import com.kltn.scsms_api_service.core.entity.SalesOrder;
import com.kltn.scsms_api_service.core.entity.SalesOrderLine;
import com.kltn.scsms_api_service.core.entity.SalesReturn;
import com.kltn.scsms_api_service.core.entity.SalesReturnLine;
import com.kltn.scsms_api_service.core.entity.enumAttribute.SalesStatus;
import com.kltn.scsms_api_service.core.entity.enumAttribute.StockRefType;
import com.kltn.scsms_api_service.core.service.entityService.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesBusinessService {
    private final SalesOrderEntityService salesOrderEntityService;
    private final SalesOrderLineEntityService solES;
    private final SalesReturnEntityService srES;
    private final SalesReturnLineEntityService srlES;
    private final InventoryBusinessService inventoryBS;
    private final PricingBusinessService pricingBS;
    private final ProductService productES;
    
    
    @Transactional
    public SalesOrder createDraft(SalesOrder so) {
        so.setStatus(SalesStatus.DRAFT);
        return salesOrderEntityService.create(so);
    }
    
    
    @Transactional
    public SalesOrder confirm(UUID soId) {
        SalesOrder so = salesOrderEntityService.require(soId);
        // price resolution + reservation
        for (SalesOrderLine line : solES.byOrder(so.getId())) {
            if (line.getUnitPrice() == null) {
                line.setUnitPrice(pricingBS.resolveUnitPrice(line.getProduct().getProductId()));
                solES.update(line);
            }
            inventoryBS.reserveStock(so.getWarehouse().getId(), line.getProduct().getProductId(), line.getQuantity(), so.getId(), StockRefType.SALE_ORDER);
        }
        so.setStatus(SalesStatus.CONFIRMED);
        return salesOrderEntityService.update(so);
    }
    
    
    @Transactional
    public SalesOrder fulfill(UUID soId) {
        SalesOrder so = salesOrderEntityService.require(soId);
        for (SalesOrderLine line : solES.byOrder(so.getId())) {
            inventoryBS.fulfillStockFIFO(so.getWarehouse().getId(), line.getProduct().getProductId(), line.getQuantity(), so.getId(), StockRefType.SALE_ORDER);
        }
        so.setStatus(SalesStatus.FULFILLED);
        return salesOrderEntityService.update(so);
    }
    
    
    @Transactional
    public SalesReturn createReturn(UUID soId, Map<UUID, Long> productQtyMap, Map<UUID, BigDecimal> unitCostOptional) {
        SalesOrder so = salesOrderEntityService.require(soId);
        so.setStatus(SalesStatus.RETURNED);
        salesOrderEntityService.update(so);
        
        SalesReturn sr = srES.create(SalesReturn.builder().salesOrder(so).warehouse(so.getWarehouse()).build());
        productQtyMap.forEach((productId, qty) -> {
            srlES.create(SalesReturnLine.builder()
                .salesReturn(sr)
                .product(productES.getRefByProductId(productId))
                .quantity(qty)
                .build());
            BigDecimal unitCost = unitCostOptional != null ? unitCostOptional.get(productId) : null;
            inventoryBS.returnToStock(so.getWarehouse().getId(), productId, qty, unitCost, sr.getId(), StockRefType.SALE_RETURN);
        });
        return sr;
    }
}
