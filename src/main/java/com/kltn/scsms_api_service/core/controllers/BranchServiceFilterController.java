package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.core.dto.branchServiceFilter.BranchServiceFilterResult;
import com.kltn.scsms_api_service.core.dto.branchServiceFilter.ServiceAvailabilityInfo;
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

@RestController
@RequestMapping("/branch-service-filter")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Branch Service Filter", description = "APIs để lọc service theo branch")
public class BranchServiceFilterController {

    private final BranchServiceFilterService branchServiceFilterService;

    /**
     * Lọc các service có thể sử dụng theo branch
     */
    @GetMapping("/branch/{branchId}/services")
    @Operation(summary = "Lọc services theo branch", description = "Lấy danh sách các service có thể sử dụng ở branch dựa trên inventory")
    public ResponseEntity<ApiResponse<BranchServiceFilterResult>> filterServicesByBranch(
            @Parameter(description = "ID của branch") @PathVariable UUID branchId,
            @Parameter(description = "Yêu cầu đủ inventory (true) hay chỉ cần có một phần (false)") @RequestParam(defaultValue = "true") boolean requireFullInventory) {

        log.info("Filtering services by branch: {} with full inventory requirement: {}", branchId,
                requireFullInventory);
        BranchServiceFilterResult result = branchServiceFilterService.filterServicesByBranch(branchId,
                requireFullInventory);
        return ResponseBuilder.success("Filtered services by branch", result);
    }

    /**
     * Lọc tất cả service có thể sử dụng theo branch
     */
    @GetMapping("/branch/{branchId}/all")
    @Operation(summary = "Lọc tất cả services theo branch", description = "Lấy danh sách tất cả service có thể sử dụng ở branch dựa trên inventory")
    public ResponseEntity<ApiResponse<BranchServiceFilterResult>> filterAllServicesByBranch(
            @Parameter(description = "ID của branch") @PathVariable UUID branchId,
            @Parameter(description = "Yêu cầu đủ inventory (true) hay chỉ cần có một phần (false)") @RequestParam(defaultValue = "true") boolean requireFullInventory) {

        log.info("Filtering all services by branch: {} with full inventory requirement: {}", branchId,
                requireFullInventory);
        BranchServiceFilterResult result = branchServiceFilterService.filterAllServicesByBranch(branchId,
                requireFullInventory);
        return ResponseBuilder.success("Filtered all services by branch", result);
    }

    /**
     * Lọc services có thể sử dụng theo branch (chỉ trả về danh sách ID)
     */
    @GetMapping("/branch/{branchId}/services/ids")
    @Operation(summary = "Lọc service IDs theo branch", description = "Lấy danh sách ID của các service có thể sử dụng ở branch")
    public ResponseEntity<ApiResponse<List<UUID>>> filterServiceIdsByBranch(
            @Parameter(description = "ID của branch") @PathVariable UUID branchId,
            @Parameter(description = "Yêu cầu đủ inventory (true) hay chỉ cần có một phần (false)") @RequestParam(defaultValue = "true") boolean requireFullInventory) {

        log.info("Filtering service IDs by branch: {} with full inventory requirement: {}", branchId,
                requireFullInventory);
        BranchServiceFilterResult result = branchServiceFilterService.filterServicesByBranch(branchId,
                requireFullInventory);

        // Extract only IDs from available services
        List<UUID> serviceIds = result.getAvailableServices().stream()
                .map(service -> service.getId())
                .toList();

        return ResponseBuilder.success("Filtered service IDs by branch", serviceIds);
    }

    /**
     * Kiểm tra tính khả dụng của một service cụ thể tại branch
     * So sánh sản phẩm dịch vụ cần dùng với kho của chi nhánh
     */
    @GetMapping("/branch/{branchId}/service/{serviceId}/availability")
    @Operation(summary = "Kiểm tra tính khả dụng của service tại branch", 
               description = "Kiểm tra xem một service cụ thể có thể sử dụng tại branch dựa trên inventory của kho chi nhánh")
    public ResponseEntity<ApiResponse<ServiceAvailabilityInfo>> checkServiceAvailability(
            @Parameter(description = "ID của branch") @PathVariable UUID branchId,
            @Parameter(description = "ID của service cần kiểm tra") @PathVariable UUID serviceId,
            @Parameter(description = "Yêu cầu đủ inventory (true) hay chỉ cần có một phần (false)") 
            @RequestParam(defaultValue = "true") boolean requireFullInventory) {

        log.info("Checking service availability - Branch: {}, Service: {}, RequireFullInventory: {}", 
                branchId, serviceId, requireFullInventory);
        
        ServiceAvailabilityInfo availabilityInfo = branchServiceFilterService
                .checkSingleServiceAvailability(branchId, serviceId, requireFullInventory);
        
        return ResponseBuilder.success("Service availability checked", availabilityInfo);
    }

    /**
     * Test endpoint để kiểm tra xem controller có hoạt động không
     */
    @GetMapping("/branch/{branchId}/test")
    @Operation(summary = "Test endpoint", description = "Test endpoint để kiểm tra controller")
    public ResponseEntity<ApiResponse<String>> testEndpoint(
            @Parameter(description = "ID của branch") @PathVariable UUID branchId) {
        log.info("Test endpoint called with branchId: {}", branchId);
        return ResponseBuilder.success("Test endpoint works! BranchId: " + branchId, "OK");
    }

    /**
     * Test POST endpoint để kiểm tra xem POST có hoạt động không
     */
    @PostMapping("/branch/{branchId}/test-post")
    @Operation(summary = "Test POST endpoint", description = "Test POST endpoint để kiểm tra POST method")
    public ResponseEntity<ApiResponse<String>> testPostEndpoint(
            @Parameter(description = "ID của branch") @PathVariable UUID branchId,
            @RequestBody(required = false) Object body) {
        log.info("Test POST endpoint called with branchId: {}, body: {}", branchId, body);
        return ResponseBuilder.success("Test POST endpoint works! BranchId: " + branchId, "OK");
    }

    /**
     * Kiểm tra tính khả dụng của nhiều services cùng lúc tại branch
     * So sánh sản phẩm dịch vụ cần dùng với kho của chi nhánh cho nhiều services
     * 
     * Sử dụng POST với request body (khuyến nghị - tránh vấn đề với List trong query param)
     */
    @PostMapping("/branch/{branchId}/batch-check-services")
    @Operation(summary = "Kiểm tra tính khả dụng của nhiều services tại branch", 
               description = "Kiểm tra nhiều services cùng lúc tại branch dựa trên inventory của kho chi nhánh. " +
                           "Truyền danh sách serviceIds trong request body (JSON array). Nếu không truyền serviceIds, trả về kết quả rỗng.")
    public ResponseEntity<ApiResponse<BranchServiceFilterResult>> checkMultipleServicesAvailability(
            @Parameter(description = "ID của branch") @PathVariable UUID branchId,
            @Parameter(description = "Danh sách ID của các service cần kiểm tra (JSON array). Có thể truyền null hoặc mảng rỗng.") 
            @RequestBody List<UUID> serviceIds,
            @Parameter(description = "Yêu cầu đủ inventory (true) hay chỉ cần có một phần (false)") 
            @RequestParam(defaultValue = "true") boolean requireFullInventory) {

        log.info("Batch checking services availability - Branch: {}, Services count: {}, RequireFullInventory: {}", 
                branchId, serviceIds != null ? serviceIds.size() : 0, requireFullInventory);
        
        BranchServiceFilterResult result = branchServiceFilterService
                .checkMultipleServicesAvailability(branchId, serviceIds, requireFullInventory);
        
        return ResponseBuilder.success("Batch service availability checked", result);
    }

}
