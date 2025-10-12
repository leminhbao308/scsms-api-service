package com.kltn.scsms_api_service.core.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kltn.scsms_api_service.core.dto.inventoryManagement.InventoryLevelInfoDto;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.entity.InventoryLevel;
import com.kltn.scsms_api_service.core.entity.enumAttribute.StockRefType;
import com.kltn.scsms_api_service.core.service.businessService.InventoryBusinessService;
import com.kltn.scsms_api_service.core.service.entityService.InventoryLevelEntityService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.InventoryLevelMapper;
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
 * Controller handling Inventory operations
 * Manages stock levels, reservations, fulfillments, and returns
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory Management", description = "Inventory management endpoints")
public class InventoryController {
    private final InventoryBusinessService inventoryBS;
    private final InventoryLevelEntityService invLevelES;
    
    private final InventoryLevelMapper invLevelMapper;
    
    
    @GetMapping("/inv/level")
    public ResponseEntity<ApiResponse<InventoryLevelInfoDto>> level(@RequestParam UUID warehouseId, @RequestParam UUID productId) {
        InventoryLevel invLevel = invLevelES.find(warehouseId, productId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Inventory level not found"));
        
        InventoryLevelInfoDto invLevelDto = invLevelMapper.toInventoryLevelInfoDto(invLevel);
        
        return ResponseBuilder.success("Fetch inventory level successfully", invLevelDto);
    }
    
    
    @PostMapping("/inv/add-stock")
    public ResponseEntity<ApiResponse<Void>> add(@RequestBody AddStockRequest req) {
        inventoryBS.addStock(req.getWarehouseId(), req.getProductId(), req.getQty(), req.getUnitCost(), req.getRefId(), req.getRefType());
        return ResponseBuilder.success("Add stock successfully");
    }
    
    
    @PostMapping("/inv/reserve")
    public ResponseEntity<ApiResponse<Void>> reserve(@RequestBody MoveRequest req) {
        inventoryBS.reserveStock(req.getWarehouseId(), req.getProductId(), req.getQty(), req.getRefId(), req.getRefType());
        return ResponseBuilder.success("Reserve stock successfully");
    }
    
    
    @PostMapping("/inv/release")
    public ResponseEntity<ApiResponse<Void>> release(@RequestBody MoveRequest req) {
        inventoryBS.releaseReservation(req.getWarehouseId(), req.getProductId(), req.getQty(), req.getRefId(), req.getRefType());
        return ResponseBuilder.success("Release reservation successfully");
    }
    
    
    @PostMapping("/inv/fulfill")
    public ResponseEntity<ApiResponse<Void>> fulfill(@RequestBody MoveRequest req) {
        inventoryBS.fulfillStockFIFO(req.getWarehouseId(), req.getProductId(), req.getQty(), req.getRefId(), req.getRefType());
        return ResponseBuilder.success("Fulfill stock successfully");
    }
    
    
    @PostMapping("/inv/return")
    public ResponseEntity<ApiResponse<Void>> returnToStock(@RequestBody AddStockRequest req) {
        inventoryBS.returnToStock(req.getWarehouseId(), req.getProductId(), req.getQty(), req.getUnitCost(), req.getRefId(), req.getRefType());
        return ResponseBuilder.success("Return to stock successfully");
    }
    
    @PostMapping("/inv/levels-batch")
    public ResponseEntity<ApiResponse<InventoryLevelsBatchResponse>> levelsBatch(@RequestBody InventoryLevelsBatchRequest req) {
        Map<UUID, InventoryView> map = new LinkedHashMap<>();
        for (UUID productId : req.getProductIds()) {
            InventoryLevel level = invLevelES.find(req.getWarehouseId(), productId).orElse(null);
            long onHand = level != null ? Optional.ofNullable(level.getOnHand()).orElse(0L) : 0L;
            long reserved = level != null ? Optional.ofNullable(level.getReserved()).orElse(0L) : 0L;
            map.put(productId, new InventoryView(onHand, reserved, onHand - reserved));
        }
        return ResponseBuilder.success("Fetch batch inventory levels successfully", new InventoryLevelsBatchResponse(map));
    }
    
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddStockRequest {
        
        @JsonProperty("warehouse_id")
        private UUID warehouseId;
        
        @JsonProperty("product_id")
        private UUID productId;
        
        private Long qty;
        
        @JsonProperty("unit_cost")
        private BigDecimal unitCost;
        
        @JsonProperty("ref_id")
        private UUID refId;
        
        @JsonProperty("ref_type")
        private StockRefType refType;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoveRequest {
        
        @JsonProperty("warehouse_id")
        private UUID warehouseId;
        
        @JsonProperty("product_id")
        private UUID productId;
        
        private Long qty;
        
        @JsonProperty("ref_id")
        private UUID refId;
        
        @JsonProperty("ref_type")
        private StockRefType refType;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryLevelsBatchRequest {
        
        @JsonProperty("warehouse_id")
        private UUID warehouseId;
        
        @JsonProperty("product_ids")
        private List<UUID> productIds;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryLevelsBatchResponse {
        
        private Map<UUID, InventoryView> items = new LinkedHashMap<>();
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryView {
        
        @JsonProperty("on_hand")
        private long onHand;
        
        @JsonProperty("reserved")
        private long reserved;
        
        @JsonProperty("available")
        private long available;
    }
}
