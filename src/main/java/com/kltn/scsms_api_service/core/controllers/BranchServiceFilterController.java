package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.core.dto.branchServiceFilter.BranchServiceFilterResult;
import com.kltn.scsms_api_service.core.service.businessService.BranchServiceFilterService;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller để lọc các service và service package có thể sử dụng theo branch
 */
@RestController
@RequestMapping("/branch-service-filter")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Branch Service Filter", description = "APIs để lọc service/service package theo branch")
public class BranchServiceFilterController {
    
    private final BranchServiceFilterService branchServiceFilterService;
    
    /**
     * Lọc các service có thể sử dụng theo branch
     */
    @GetMapping("/branch/{branchId}/services")
    @Operation(summary = "Lọc services theo branch", 
               description = "Lấy danh sách các service có thể sử dụng ở branch dựa trên inventory")
    public ResponseEntity<ApiResponse<BranchServiceFilterResult>> filterServicesByBranch(
            @Parameter(description = "ID của branch") @PathVariable UUID branchId,
            @Parameter(description = "Yêu cầu đủ inventory (true) hay chỉ cần có một phần (false)") 
            @RequestParam(defaultValue = "true") boolean requireFullInventory) {
        
        log.info("Filtering services by branch: {} with full inventory requirement: {}", branchId, requireFullInventory);
        BranchServiceFilterResult result = branchServiceFilterService.filterServicesByBranch(branchId, requireFullInventory);
        return ResponseBuilder.success("Filtered services by branch", result);
    }
    
    /**
     * Lọc các service package có thể sử dụng theo branch
     */
    @GetMapping("/branch/{branchId}/service-packages")
    @Operation(summary = "Lọc service packages theo branch", 
               description = "Lấy danh sách các service package có thể sử dụng ở branch dựa trên inventory")
    public ResponseEntity<ApiResponse<BranchServiceFilterResult>> filterServicePackagesByBranch(
            @Parameter(description = "ID của branch") @PathVariable UUID branchId,
            @Parameter(description = "Yêu cầu đủ inventory (true) hay chỉ cần có một phần (false)") 
            @RequestParam(defaultValue = "true") boolean requireFullInventory) {
        
        log.info("Filtering service packages by branch: {} with full inventory requirement: {}", branchId, requireFullInventory);
        BranchServiceFilterResult result = branchServiceFilterService.filterServicePackagesByBranch(branchId, requireFullInventory);
        return ResponseBuilder.success("Filtered service packages by branch", result);
    }
    
    /**
     * Lọc cả service và service package có thể sử dụng theo branch
     */
    @GetMapping("/branch/{branchId}/all")
    @Operation(summary = "Lọc tất cả services và packages theo branch", 
               description = "Lấy danh sách tất cả service và service package có thể sử dụng ở branch dựa trên inventory")
    public ResponseEntity<ApiResponse<BranchServiceFilterResult>> filterAllServicesByBranch(
            @Parameter(description = "ID của branch") @PathVariable UUID branchId,
            @Parameter(description = "Yêu cầu đủ inventory (true) hay chỉ cần có một phần (false)") 
            @RequestParam(defaultValue = "true") boolean requireFullInventory) {
        
        log.info("Filtering all services and packages by branch: {} with full inventory requirement: {}", branchId, requireFullInventory);
        BranchServiceFilterResult result = branchServiceFilterService.filterAllServicesByBranch(branchId, requireFullInventory);
        return ResponseBuilder.success("Filtered all services and packages by branch", result);
    }
    
    /**
     * Lọc services có thể sử dụng theo branch (chỉ trả về danh sách ID)
     */
    @GetMapping("/branch/{branchId}/services/ids")
    @Operation(summary = "Lọc service IDs theo branch", 
               description = "Lấy danh sách ID của các service có thể sử dụng ở branch")
    public ResponseEntity<ApiResponse<List<UUID>>> filterServiceIdsByBranch(
            @Parameter(description = "ID của branch") @PathVariable UUID branchId,
            @Parameter(description = "Yêu cầu đủ inventory (true) hay chỉ cần có một phần (false)") 
            @RequestParam(defaultValue = "true") boolean requireFullInventory) {
        
        log.info("Filtering service IDs by branch: {} with full inventory requirement: {}", branchId, requireFullInventory);
        BranchServiceFilterResult result = branchServiceFilterService.filterServicesByBranch(branchId, requireFullInventory);
        
        // Extract only IDs from available services
        List<UUID> serviceIds = result.getAvailableServices().stream()
            .map(service -> service.getId())
            .toList();
        
        return ResponseBuilder.success("Filtered service IDs by branch", serviceIds);
    }
    
    /**
     * Lọc service package IDs có thể sử dụng theo branch (chỉ trả về danh sách ID)
     */
    @GetMapping("/branch/{branchId}/service-packages/ids")
    @Operation(summary = "Lọc service package IDs theo branch", 
               description = "Lấy danh sách ID của các service package có thể sử dụng ở branch")
    public ResponseEntity<ApiResponse<List<UUID>>> filterServicePackageIdsByBranch(
            @Parameter(description = "ID của branch") @PathVariable UUID branchId,
            @Parameter(description = "Yêu cầu đủ inventory (true) hay chỉ cần có một phần (false)") 
            @RequestParam(defaultValue = "true") boolean requireFullInventory) {
        
        log.info("Filtering service package IDs by branch: {} with full inventory requirement: {}", branchId, requireFullInventory);
        BranchServiceFilterResult result = branchServiceFilterService.filterServicePackagesByBranch(branchId, requireFullInventory);
        
        // Extract only IDs from available service packages
        List<UUID> packageIds = result.getAvailableServices().stream()
            .map(packageInfo -> packageInfo.getId())
            .toList();
        
        return ResponseBuilder.success("Filtered service package IDs by branch", packageIds);
    }
}
