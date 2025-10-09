package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.PurchaseOrderInfoDto;
import com.kltn.scsms_api_service.core.dto.purchaseOrderManagement.request.CreatePORequest;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.entity.PurchaseOrder;
import com.kltn.scsms_api_service.core.service.businessService.PurchasingBusinessService;
import com.kltn.scsms_api_service.core.service.entityService.BranchService;
import com.kltn.scsms_api_service.core.service.entityService.PurchaseOrderEntityService;
import com.kltn.scsms_api_service.core.service.entityService.PurchaseOrderLineEntityService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import com.kltn.scsms_api_service.mapper.PurchaseOrderLineMapper;
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
    
    private final PurchaseOrderMapper poMapper;
    
    @PostMapping("/po/create-po")
    @SwaggerOperation(
        summary = "Create purchase order",
        description = "Create a new purchase order with specified details")
    public ResponseEntity<ApiResponse<PurchaseOrderInfoDto>> createDraft(@RequestBody CreatePORequest req) {
        PurchaseOrder po = purchasingBS.createDraft(req);
        
        PurchaseOrderInfoDto poDto = poMapper.toPurchaseOrderInfoDto(poES.require(po.getId()));
        return ResponseBuilder.success("Purchase order draft created", poDto);
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
