package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.PurchaseOrderInfoDto;
import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.request.CreatePOLine;
import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.request.CreatePORequest;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.entity.PurchaseOrder;
import com.kltn.scsms_api_service.core.entity.PurchaseOrderLine;
import com.kltn.scsms_api_service.core.entity.enumAttribute.PurchaseStatus;
import com.kltn.scsms_api_service.core.service.businessService.PurchasingBusinessService;
import com.kltn.scsms_api_service.core.service.entityService.*;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.PurchaseOrderMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    
    private final PurchaseOrderMapper poMapper;
    
    @PostMapping("/po/create-draft")
    @SwaggerOperation(
        summary = "Create purchase order draft",
        description = "Create a new purchase order in draft status with specified details")
    public ResponseEntity<ApiResponse<PurchaseOrderInfoDto>> createDraft(@RequestBody CreatePORequest req) {
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
                .product(productES.getRefByProductId(l.getProductId()))
                .supplier(supplierES.getRefById(l.getSupplierId()))
                .quantityOrdered(l.getQty())
                .unitCost(l.getUnitCost())
                .lotCode(l.getLotCode())
                .expiryDate(l.getExpiryDate())
                .build());
        }
        
        PurchaseOrderInfoDto poDto = poMapper.toPurchaseOrderInfoDto(poES.require(po.getId()));
        return ResponseBuilder.success("Purchase order draft created", poDto);
    }
    
    
    @PostMapping("/po/submit/{poId}")
    @SwaggerOperation(
        summary = "Submit purchase order",
        description = "Submit a draft purchase order for processing")
    public ResponseEntity<ApiResponse<PurchaseOrderInfoDto>> submit(@PathVariable UUID poId) {
        PurchaseOrder po = poES.require(poId);
        if (po.getStatus() != PurchaseStatus.DRAFT) {
            throw new ClientSideException(ErrorCode.BAD_REQUEST, "Only draft purchase orders can be submitted");
        }
        
        PurchaseOrderInfoDto poDto = poMapper.toPurchaseOrderInfoDto(purchasingBS.submit(po));
        return ResponseBuilder.success("Purchase order submitted", poDto);
    }
    
    
    @PostMapping("/po/receive/{poId}")
    @SwaggerOperation(
        summary = "Receive entire purchase order",
        description = "Mark the entire purchase order as received and update inventory accordingly")
    public ResponseEntity<ApiResponse<PurchaseOrderInfoDto>> receiveAll(@PathVariable UUID poId) {
        PurchaseOrder po = purchasingBS.receive(poId);
        
        PurchaseOrderInfoDto poDto = poMapper.toPurchaseOrderInfoDto(po);
        
        return ResponseBuilder.success("Purchase order received", poDto);
    }
    
    @PostMapping("/po/cancel/{poId}")
    @SwaggerOperation(
        summary = "Cancel purchase order",
        description = "Cancel a purchase order that is not yet fully received")
    public ResponseEntity<ApiResponse<PurchaseOrderInfoDto>> cancel(@PathVariable UUID poId) {
        PurchaseOrder po = purchasingBS.cancel(poId);
        
        PurchaseOrderInfoDto poDto = poMapper.toPurchaseOrderInfoDto(po);
        
        return ResponseBuilder.success("Purchase order cancelled", poDto);
    }
    
    
    @GetMapping("/po/{poId}")
    @SwaggerOperation(
        summary = "Get purchase order by ID",
        description = "Retrieve purchase order details using its unique identifier")
    public ResponseEntity<ApiResponse<PurchaseOrderInfoDto>> get(@PathVariable UUID poId) {
        PurchaseOrder po = poES.require(poId);
        
        PurchaseOrderInfoDto poDto = poMapper.toPurchaseOrderInfoDto(po);
        
        return ResponseBuilder.success("Purchase order retrieved", poDto);
    }
    
    @GetMapping("/po/get-all")
    @SwaggerOperation(
        summary = "Get all purchase orders",
        description = "Retrieve a list of all purchase orders in the system")
    public ResponseEntity<ApiResponse<List<PurchaseOrderInfoDto>>> getAll() {
        List<PurchaseOrder> pos = poES.getAll();
        
        List<PurchaseOrderInfoDto> posDto = pos.stream()
            .map(poMapper::toPurchaseOrderInfoDto).collect(Collectors.toList());
        
        return ResponseBuilder.success("All purchase orders retrieved", posDto);
    }
    
}
