package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.saleOrderManagement.SaleOrderInfoDto;
import com.kltn.scsms_api_service.core.entity.SalesOrder;
import com.kltn.scsms_api_service.core.entity.SalesOrderLine;
import com.kltn.scsms_api_service.core.entity.SalesReturn;
import com.kltn.scsms_api_service.core.entity.User;
import com.kltn.scsms_api_service.core.entity.enumAttribute.SalesStatus;
import com.kltn.scsms_api_service.core.service.businessService.SalesBusinessService;
import com.kltn.scsms_api_service.core.service.entityService.*;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import com.kltn.scsms_api_service.mapper.SaleOrderMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Controller handling Sales Order operations
 * Manages creation, confirmation, fulfillment, and returns of sales orders
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sales Order Management", description = "Sale order management endpoints")
public class SalesOrdersController {
    private final SalesBusinessService salesBS;
    private final SalesOrderEntityService soES;
    private final SalesOrderLineEntityService solES;
    
    private final ProductService productES;
    private final BranchService branchES;
    private final WarehouseEntityService warehouseES;
    private final UserService userES;
    
    private final SaleOrderMapper soMapper;
    
    @PostMapping("/so/create-draft")
    public ResponseEntity<ApiResponse<SaleOrderInfoDto>> createDraft(@RequestBody CreateSORequest req) {
        User customer = null;
        if (req.getCustomerId() != null)
            customer = userES.getUserRefById(req.getCustomerId());
        
        SalesOrder so = SalesOrder.builder()
            .branch(branchES.getRefById(req.getBranchId()))
            .warehouse(warehouseES.getRefByWarehouseId(req.getWarehouseId()))
            .customer(customer)
            .status(SalesStatus.DRAFT)
            .build();
        so = salesBS.createDraft(so);
        
        List<SalesOrderLine> createdLLines = new ArrayList<>();
        for (CreateSOLine l : req.getLines()) {
            createdLLines.add(solES.create(SalesOrderLine.builder()
                .salesOrder(so)
                .product(productES.getRefByProductId(l.productId))
                .quantity(l.getQty())
                .unitPrice(l.getUnitPrice())
                .build()));
        }
        so.setLines(createdLLines);
        
        SaleOrderInfoDto soDto = soMapper.toSaleOrderInfoDto(so);
        
        return ResponseBuilder.success("Sales order draft created", soDto);
    }
    
    
    @PostMapping("/so/confirm/{soId}")
    public ResponseEntity<ApiResponse<SaleOrderInfoDto>> confirm(@PathVariable UUID soId) {
        SalesOrder so = salesBS.confirm(soId);
        
        SaleOrderInfoDto soDto = soMapper.toSaleOrderInfoDto(so);
        
        return ResponseBuilder.success("Sales order confirmed", soDto);
    }
    
    
    @PostMapping("/so/fulfill/{soId}")
    public ResponseEntity<ApiResponse<SalesOrder>> fulfill(@PathVariable UUID soId) {
        return ResponseBuilder.success("Sales order fulfilled", salesBS.fulfill(soId));
    }
    
    
    @PostMapping("/so/return/{soId}")
    public ResponseEntity<ApiResponse<SalesReturn>> createReturn(@PathVariable UUID soId, @RequestBody CreateReturnRequest req) {
        Map<UUID, Long> items = new LinkedHashMap<>();
        Map<UUID, BigDecimal> unitCosts = new LinkedHashMap<>();
        for (ReturnItem i : req.getItems()) {
            items.put(i.getProductId(), i.getQty());
            if (i.getUnitCost() != null) unitCosts.put(i.getProductId(), i.getUnitCost());
        }
        return ResponseBuilder.success("Sales return created", salesBS.createReturn(soId, items, unitCosts));
    }
    
    
    @GetMapping("/so/{soId}")
    public ResponseEntity<ApiResponse<SalesOrder>> get(@PathVariable UUID soId) {
        return ResponseBuilder.success(soES.require(soId));
    }
    
    @GetMapping("/so/get-all")
    public ResponseEntity<ApiResponse<List<SalesOrder>>> getAll() {
        return ResponseBuilder.success(soES.getAll());
    }
    
    // ===== DTOs =====
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateSORequest {
        private UUID branchId;
        private UUID warehouseId;
        private UUID customerId; // optional
        private List<CreateSOLine> lines;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateSOLine {
        private UUID productId;
        private Long qty;
        private BigDecimal unitPrice; // optional, auto-resolve if null
    }
    
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateReturnRequest {
        private List<ReturnItem> items;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnItem {
        private UUID productId;
        private Long qty;
        private BigDecimal unitCost;
    }
}
