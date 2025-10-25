package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.serviceBayManagement.ServiceBayInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceBayManagement.ServiceBayStatisticsDto;
import com.kltn.scsms_api_service.core.dto.serviceBayManagement.TechnicianInfoDto;
import com.kltn.scsms_api_service.core.dto.bookingManagement.BookingInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceBayManagement.request.BayAvailabilityRequest;
import com.kltn.scsms_api_service.core.dto.serviceBayManagement.request.CreateServiceBayRequest;
import com.kltn.scsms_api_service.core.dto.serviceBayManagement.request.ServiceBayFilterParam;
import com.kltn.scsms_api_service.core.dto.serviceBayManagement.request.UpdateServiceBayRequest;
import com.kltn.scsms_api_service.core.entity.ServiceBay;
import com.kltn.scsms_api_service.core.service.businessService.ServiceBayManagementService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller quản lý Service Bay
 */
@Slf4j
@RestController
@RequestMapping(ApiConstant.SERVICE_BAY_MANAGEMENT_PREFIX)
@RequiredArgsConstructor
@Tag(name = "Service Bay Management", description = "APIs for managing service bays")
public class ServiceBayManagementController {

    private final ServiceBayManagementService serviceBayManagementService;

    @GetMapping("/get-all")
    @Operation(summary = "Get all service bays", description = "Get all service bays with filtering and pagination")
    @SwaggerOperation(summary = "Get all service bays")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_BAY_READ)
    public ResponseEntity<ApiResponse<Page<ServiceBayInfoDto>>> getAllServiceBays(
            @Parameter(description = "Filter parameters") ServiceBayFilterParam filterParam) {
        Page<ServiceBayInfoDto> bays = serviceBayManagementService.getAllServiceBays(filterParam);
        return ResponseBuilder.success(bays);
    }

    @GetMapping("/dropdown")
    @Operation(summary = "Get service bays dropdown", description = "Get service bays for dropdown selection")
    @SwaggerOperation(summary = "Get service bays dropdown")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_BAY_READ)
    public ResponseEntity<ApiResponse<List<ServiceBayInfoDto>>> getServiceBaysDropdown(
            @Parameter(description = "Branch ID") @RequestParam(required = false) UUID branchId) {
        List<ServiceBayInfoDto> bays = serviceBayManagementService.getServiceBaysDropdown(branchId);
        return ResponseBuilder.success(bays);
    }

    @GetMapping("/{bayId}")
    @Operation(summary = "Get service bay by ID", description = "Get service bay details by ID")
    @SwaggerOperation(summary = "Get service bay by ID")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_BAY_READ)
    public ResponseEntity<ApiResponse<ServiceBayInfoDto>> getServiceBayById(
            @Parameter(description = "Bay ID") @PathVariable UUID bayId) {
        ServiceBayInfoDto bay = serviceBayManagementService.getServiceBayById(bayId);
        return ResponseBuilder.success(bay);
    }

    @GetMapping("/branch/{branchId}")
    @Operation(summary = "Get service bays by branch", description = "Get all service bays for a specific branch")
    @SwaggerOperation(summary = "Get service bays by branch")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_BAY_READ)
    public ResponseEntity<ApiResponse<List<ServiceBayInfoDto>>> getServiceBaysByBranch(
            @Parameter(description = "Branch ID") @PathVariable UUID branchId) {
        List<ServiceBayInfoDto> bays = serviceBayManagementService.getServiceBaysByBranch(branchId);
        return ResponseBuilder.success(bays);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active service bays", description = "Get all active service bays")
    @SwaggerOperation(summary = "Get active service bays")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_BAY_READ)
    public ResponseEntity<ApiResponse<List<ServiceBayInfoDto>>> getActiveServiceBays(
            @Parameter(description = "Branch ID") @RequestParam(required = false) UUID branchId) {
        log.info("Getting active service bays for branch: {}", branchId);
        List<ServiceBayInfoDto> bays = serviceBayManagementService.getActiveServiceBays(branchId);
        return ResponseBuilder.success(bays);
    }

    @GetMapping("/available")
    @Operation(summary = "Get available service bays", description = "Get available service bays in time range")
    @SwaggerOperation(summary = "Get available service bays")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_BAY_READ)
    public ResponseEntity<ApiResponse<List<ServiceBayInfoDto>>> getAvailableServiceBays(
            @Parameter(description = "Branch ID") @RequestParam UUID branchId,
            @Parameter(description = "Start time") @RequestParam String startTime,
            @Parameter(description = "End time") @RequestParam String endTime) {
        log.info("Getting available service bays for branch: {} from {} to {}", branchId, startTime, endTime);
        List<ServiceBayInfoDto> bays = serviceBayManagementService.getAvailableServiceBays(branchId, startTime,
                endTime);
        return ResponseBuilder.success(bays);
    }

    @GetMapping("/search")
    @Operation(summary = "Search service bays", description = "Search service bays by keyword")
    @SwaggerOperation(summary = "Search service bays")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_BAY_READ)
    public ResponseEntity<ApiResponse<List<ServiceBayInfoDto>>> searchServiceBays(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @Parameter(description = "Branch ID") @RequestParam(required = false) UUID branchId) {
        log.info("Searching service bays with keyword: {} in branch: {}", keyword, branchId);
        List<ServiceBayInfoDto> bays = serviceBayManagementService.searchServiceBays(keyword, branchId);
        return ResponseBuilder.success(bays);
    }

    @PostMapping("/create")
    @Operation(summary = "Create service bay", description = "Create a new service bay")
    @SwaggerOperation(summary = "Create service bay")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_BAY_CREATE)
    public ResponseEntity<ApiResponse<ServiceBayInfoDto>> createServiceBay(
            @Parameter(description = "Service bay creation request") @Valid @RequestBody CreateServiceBayRequest request) {
        ServiceBayInfoDto bay = serviceBayManagementService.createServiceBay(request);
        return ResponseBuilder.created(bay);
    }

    @PostMapping("/{bayId}/update")
    @Operation(summary = "Update service bay", description = "Update an existing service bay")
    @SwaggerOperation(summary = "Update service bay")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_BAY_UPDATE)
    public ResponseEntity<ApiResponse<ServiceBayInfoDto>> updateServiceBay(
            @Parameter(description = "Bay ID") @PathVariable UUID bayId,
            @Parameter(description = "Service bay update request") @Valid @RequestBody UpdateServiceBayRequest request) {
        ServiceBayInfoDto bay = serviceBayManagementService.updateServiceBay(bayId, request);
        return ResponseBuilder.success(bay);
    }

    @PostMapping("/{bayId}/delete")
    @Operation(summary = "Delete service bay", description = "Delete a service bay")
    @SwaggerOperation(summary = "Delete service bay")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_BAY_DELETE)
    public ResponseEntity<ApiResponse<Void>> deleteServiceBay(
            @Parameter(description = "Bay ID") @PathVariable UUID bayId) {
        serviceBayManagementService.deleteServiceBay(bayId);
        return ResponseBuilder.success("Service bay deleted successfully");
    }

    @PostMapping("/{bayId}/status")
    @Operation(summary = "Update service bay status", description = "Update service bay status")
    @SwaggerOperation(summary = "Update service bay status")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_BAY_UPDATE)
    public ResponseEntity<ApiResponse<ServiceBayInfoDto>> updateServiceBayStatus(
            @Parameter(description = "Bay ID") @PathVariable UUID bayId,
            @Parameter(description = "New status") @RequestParam ServiceBay.BayStatus status,
            @Parameter(description = "Reason") @RequestParam(required = false) String reason) {
        ServiceBayInfoDto bay = serviceBayManagementService.updateServiceBayStatus(bayId, status, reason);
        return ResponseBuilder.success(bay);
    }

    @PostMapping("/{bayId}/activate")
    @Operation(summary = "Activate service bay", description = "Activate a service bay")
    @SwaggerOperation(summary = "Activate service bay")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_BAY_UPDATE)
    public ResponseEntity<ApiResponse<ServiceBayInfoDto>> activateServiceBay(
            @Parameter(description = "Bay ID") @PathVariable UUID bayId) {
        ServiceBayInfoDto bay = serviceBayManagementService.activateServiceBay(bayId);
        return ResponseBuilder.success(bay);
    }

    @PostMapping("/{bayId}/deactivate")
    @Operation(summary = "Deactivate service bay", description = "Deactivate a service bay")
    @SwaggerOperation(summary = "Deactivate service bay")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_BAY_UPDATE)
    public ResponseEntity<ApiResponse<ServiceBayInfoDto>> deactivateServiceBay(
            @Parameter(description = "Bay ID") @PathVariable UUID bayId,
            @Parameter(description = "Reason") @RequestParam String reason) {
        ServiceBayInfoDto bay = serviceBayManagementService.deactivateServiceBay(bayId, reason);
        return ResponseBuilder.success(bay);
    }

    @PostMapping("/{bayId}/availability")
    @Operation(summary = "Check bay availability", description = "Check if a service bay is available in time range")
    @SwaggerOperation(summary = "Check bay availability")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_BAY_READ)
    public ResponseEntity<ApiResponse<Boolean>> checkBayAvailability(
            @Parameter(description = "Bay ID") @PathVariable UUID bayId,
            @Parameter(description = "Availability request") @Valid @RequestBody BayAvailabilityRequest request) {
        Boolean isAvailable = serviceBayManagementService.checkBayAvailability(bayId, request);
        return ResponseBuilder.success(isAvailable);
    }

    @GetMapping("/{bayId}/bookings")
    @Operation(summary = "Get bay bookings", description = "Get all bookings for a service bay")
    @SwaggerOperation(summary = "Get bay bookings")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_BAY_READ)
    public ResponseEntity<ApiResponse<List<BookingInfoDto>>> getBayBookings(
            @Parameter(description = "Bay ID") @PathVariable UUID bayId) {
        List<BookingInfoDto> bookings = serviceBayManagementService.getBayBookings(bayId);
        return ResponseBuilder.success(bookings);
    }

    @GetMapping("/{bayId}/statistics")
    @Operation(summary = "Get bay statistics", description = "Get statistics for a service bay")
    @SwaggerOperation(summary = "Get bay statistics")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_BAY_READ)
    public ResponseEntity<ApiResponse<ServiceBayStatisticsDto>> getBayStatistics(
            @Parameter(description = "Bay ID") @PathVariable UUID bayId) {
        ServiceBayStatisticsDto statistics = serviceBayManagementService.getBayStatistics(bayId);
        return ResponseBuilder.success(statistics);
    }

    @GetMapping("/validate-name")
    @Operation(summary = "Validate bay name", description = "Validate if bay name is available")
    @SwaggerOperation(summary = "Validate bay name")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_BAY_READ)
    public ResponseEntity<ApiResponse<Boolean>> validateBayName(
            @Parameter(description = "Branch ID") @RequestParam UUID branchId,
            @Parameter(description = "Bay name") @RequestParam String bayName,
            @Parameter(description = "Bay ID to exclude") @RequestParam(required = false) UUID bayId) {
        Boolean isValid = serviceBayManagementService.validateBayName(branchId, bayName, bayId);
        return ResponseBuilder.success(isValid);
    }

    /**
     * Lấy danh sách tất cả users (kỹ thuật viên) để chọn gán vào service bay
     */
    @GetMapping("/available-technicians")
    @Operation(summary = "Get available technicians", description = "Get list of all users that can be assigned to service bays")
    @SwaggerOperation(summary = "Get available technicians")
    // @RequirePermission(permissions = PermissionConstant.SERVICE_BAY_READ)
    public ResponseEntity<ApiResponse<List<TechnicianInfoDto>>> getAvailableTechnicians() {
        List<TechnicianInfoDto> technicians = serviceBayManagementService.getAllTechnicians();
        return ResponseBuilder.success(technicians);
    }
}
