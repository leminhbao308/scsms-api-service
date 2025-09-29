package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.abstracts.BaseResponse;
import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.dto.inventoryManagement.param.InventoryFilterParam;
import com.kltn.scsms_api_service.core.dto.inventoryManagement.request.InventoryHeaderRequest;
import com.kltn.scsms_api_service.core.dto.inventoryManagement.response.InventoryHeaderResponse;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.response.PaginatedResponse;
import com.kltn.scsms_api_service.core.service.InventoryService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventories")
public class InventoryController {

    private final InventoryService inventoryService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SwaggerOperation(summary = "Create a new inventory transaction", description = "Create a new inventory transaction for inbound, outbound, or other transaction types")
    public ResponseEntity<ApiResponse<InventoryHeaderResponse>> createInventory(@RequestBody InventoryHeaderRequest request) {
        return ResponseBuilder.success(inventoryService.createInventory(request));
    }
    
    @GetMapping("/{id}")
    @SwaggerOperation(summary = "Get inventory by ID", description = "Retrieve inventory transaction details by ID")
    public ResponseEntity<ApiResponse<InventoryHeaderResponse>> getInventoryById(@PathVariable("id") UUID inventoryId) {
        return ResponseBuilder.success(inventoryService.getInventoryById(inventoryId));
    }
    
    @GetMapping("/code/{code}")
    @SwaggerOperation(summary = "Get inventory by code", description = "Retrieve inventory transaction details by inventory code")
    public ResponseEntity<ApiResponse<InventoryHeaderResponse>> getInventoryByCode(@PathVariable("code") String inventoryCode) {
        return ResponseBuilder.success(inventoryService.getInventoryByCode(inventoryCode));
    }
    
    @PutMapping("/{id}")
    @SwaggerOperation(summary = "Update inventory", description = "Update an existing inventory transaction")
    public ResponseEntity<ApiResponse<InventoryHeaderResponse>> updateInventory(
            @PathVariable("id") UUID inventoryId,
            @RequestBody InventoryHeaderRequest request) {
        return ResponseBuilder.success(inventoryService.updateInventory(inventoryId, request));
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SwaggerOperation(summary = "Delete inventory", description = "Delete an inventory transaction (only allowed for DRAFT status)")
    public void deleteInventory(@PathVariable("id") UUID inventoryId) {
        inventoryService.deleteInventory(inventoryId);
    }
    
    @GetMapping
    @SwaggerOperation(summary = "Search inventories", description = "Search and filter inventory transactions")
    public ResponseEntity<ApiResponse<PaginatedResponse<InventoryHeaderResponse>>> searchInventories(InventoryFilterParam filterParam) {
        return ResponseBuilder.paginated(inventoryService.searchInventories(filterParam));
    }
    
    @PatchMapping("/{id}/status")
    @SwaggerOperation(summary = "Update inventory status", description = "Update the status of an inventory transaction (e.g., approve, reject, complete)")
    public ResponseEntity<ApiResponse<InventoryHeaderResponse>> updateInventoryStatus(
            @PathVariable("id") UUID inventoryId,
            @RequestParam String status) {
        return ResponseBuilder.success(inventoryService.updateInventoryStatus(inventoryId, status));
    }
}
