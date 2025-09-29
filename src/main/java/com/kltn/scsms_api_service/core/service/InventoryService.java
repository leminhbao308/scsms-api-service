package com.kltn.scsms_api_service.core.service;

import com.kltn.scsms_api_service.core.dto.inventoryManagement.param.InventoryFilterParam;
import com.kltn.scsms_api_service.core.dto.inventoryManagement.request.InventoryHeaderRequest;
import com.kltn.scsms_api_service.core.dto.inventoryManagement.response.InventoryHeaderResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface InventoryService {
    
    // Create a new inventory transaction
    InventoryHeaderResponse createInventory(InventoryHeaderRequest request);
    
    // Get inventory transaction by ID
    InventoryHeaderResponse getInventoryById(UUID inventoryId);
    
    // Get inventory transaction by code
    InventoryHeaderResponse getInventoryByCode(String inventoryCode);
    
    // Update an inventory transaction
    InventoryHeaderResponse updateInventory(UUID inventoryId, InventoryHeaderRequest request);
    
    // Delete an inventory transaction
    void deleteInventory(UUID inventoryId);
    
    // Search and filter inventory transactions
    Page<InventoryHeaderResponse> searchInventories(InventoryFilterParam filterParam);
    
    // Update inventory status (e.g., approve, reject, complete)
    InventoryHeaderResponse updateInventoryStatus(UUID inventoryId, String status);
}
