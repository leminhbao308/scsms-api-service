package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.entity.PurchaseOrder;
import com.kltn.scsms_api_service.core.entity.PurchaseOrderLine;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PurchaseStatus;
import com.kltn.scsms_api_service.core.service.businessService.PurchasingBusinessService;
import com.kltn.scsms_api_service.core.service.entityService.*;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Controller handling Purchase Order operations
 * Manages creation, submission, and receiving of purchase orders
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Purchase Order Management", description = "Purchase order management endpoints")
public class PurchaseOrdersController {
    
    private final PurchasingBusinessService purchasingBS;
    private final PurchaseOrderEntityService poES;
    private final PurchaseOrderLineEntityService polES;
    
    private final ProductService productES;
    private final BranchService branchES;
    private final WarehouseEntityService warehouseES;
    private final SupplierService supplierES;
    
    @PostMapping("/po/create-draft")
    @SwaggerOperation(
        summary = "Create purchase order draft",
        description = "Create a new purchase order in draft status with specified details")
    public ResponseEntity<ApiResponse<PurchaseOrder>> createDraft(@RequestBody CreatePORequest req) {
        PurchaseOrder po = PurchaseOrder.builder()
            .branch(branchES.getRefById(req.getBranchId()))
            .warehouse(warehouseES.getRefByWarehouseId(req.getWarehouseId()))
            .status(PurchaseStatus.DRAFT)
            .expectedAt(req.getExpectedAt())
            .build();
        po = purchasingBS.createDraft(po);
        for (CreatePOLine l : req.getLines()) {
            polES.create(PurchaseOrderLine.builder()
                .purchaseOrder(po)
                .product(productES.getRefByProductId(l.productId))
                .supplier(supplierES.getRefById(l.getSupplierId()))
                .quantityOrdered(l.getQty())
                .unitCost(l.getUnitCost())
                .lotCode(l.getLotCode())
                .expiryDate(l.getExpiryDate())
                .build());
        }
        return ResponseBuilder.success("Purchase order draft created", poES.require(po.getId()));
    }
    
    
    @PostMapping("/po/submit/{poId}")
    @SwaggerOperation(
        summary = "Submit purchase order",
        description = "Submit a draft purchase order for processing")
    public ResponseEntity<ApiResponse<PurchaseOrder>> submit(@PathVariable UUID poId) {
        PurchaseOrder po = poES.require(poId);
        return ResponseBuilder.success("Purchase order submitted", purchasingBS.submit(po));
    }
    
    
    @PostMapping("/po/receive-all/{poId}")
    @SwaggerOperation(
        summary = "Receive entire purchase order",
        description = "Mark the entire purchase order as received and update inventory accordingly")
    public ResponseEntity<ApiResponse<PurchaseOrder>> receiveAll(@PathVariable UUID poId) {
        return ResponseBuilder.success("Purchase order received", purchasingBS.receive(poId));
    }
    
    
    @GetMapping("/po/{poId}")
    @SwaggerOperation(
        summary = "Get purchase order by ID",
        description = "Retrieve purchase order details using its unique identifier")
    public ResponseEntity<ApiResponse<PurchaseOrder>> get(@PathVariable UUID poId) {
        return ResponseBuilder.success("Purchase order retrieved", poES.require(poId));
    }
    
    
    // ===== DTOs =====
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePORequest {
        private UUID branchId;
        private UUID warehouseId;
        private LocalDateTime expectedAt;
        private List<CreatePOLine> lines;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePOLine {
        private UUID productId;
        private UUID supplierId;
        private Long qty;
        private BigDecimal unitCost;
        private String lotCode;
        private LocalDateTime expiryDate;
    }
}
