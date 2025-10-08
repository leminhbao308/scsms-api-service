package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.warehouseManagement.response.WarehouseInfoDto;
import com.kltn.scsms_api_service.core.entity.Warehouse;
import com.kltn.scsms_api_service.core.service.entityService.WarehouseEntityService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import com.kltn.scsms_api_service.exception.ClientSideException;
import com.kltn.scsms_api_service.exception.ErrorCode;
import com.kltn.scsms_api_service.mapper.WarehouseMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controller handling warehouse operations
 * Manages retrieval of warehouse information
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Warehouse Management", description = "Warehouse management endpoints")
public class WarehouseController {
    
    private final WarehouseEntityService warehouseES;
    
    private final WarehouseMapper warehouseMapper;
    
    @GetMapping("/warehouses/{warehouseId}")
    @SwaggerOperation(
        summary = "Get warehouse by ID",
        description = "Retrieve warehouse details using its unique identifier")
    public ResponseEntity<ApiResponse<WarehouseInfoDto>> get(@PathVariable UUID warehouseId) {
        Warehouse warehouse = warehouseES.find(warehouseId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Warehouse not found"));
        
        WarehouseInfoDto warehouseDto = warehouseMapper.toWarehouseInfoDto(warehouse);
        
        return ResponseBuilder.success("Warehouse retrieved successfully", warehouseDto);
    }
    
    
    @GetMapping("/warehouses/by-branch/{branchId}")
    @SwaggerOperation(
        summary = "Get warehouse by branch ID",
        description = "Retrieve warehouse details associated with a specific branch")
    public ResponseEntity<ApiResponse<WarehouseInfoDto>> byBranch(@PathVariable UUID branchId) {
        Warehouse warehouse = warehouseES.findByBranch(branchId)
            .orElseThrow(() -> new ClientSideException(ErrorCode.NOT_FOUND, "Branch not found"));
        
        WarehouseInfoDto warehouseDto = warehouseMapper.toWarehouseInfoDto(warehouse);
        
        return ResponseBuilder.success("Warehouse retrieved successfully", warehouseDto);
    }
    
    @GetMapping("/warehouses/get-all")
    @SwaggerOperation(
        summary = "Get all warehouses",
        description = "Retrieve a list of all warehouses in the system")
    public ResponseEntity<ApiResponse<List<WarehouseInfoDto>>> getAll() {
        List<Warehouse> warehouses = warehouseES.findAll();
        List<WarehouseInfoDto> warehouseDtos = warehouses.stream()
            .map(warehouseMapper::toWarehouseInfoDto)
            .toList();
        
        return ResponseBuilder.success("Warehouses retrieved successfully", warehouseDtos);
    }
}
