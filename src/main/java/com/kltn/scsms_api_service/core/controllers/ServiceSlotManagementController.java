package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.dto.serviceSlotManagement.ServiceSlotInfoDto;
import com.kltn.scsms_api_service.core.dto.serviceSlotManagement.param.ServiceSlotFilterParam;
import com.kltn.scsms_api_service.core.dto.serviceSlotManagement.request.CreateServiceSlotRequest;
import com.kltn.scsms_api_service.core.dto.serviceSlotManagement.request.UpdateServiceSlotRequest;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.entity.ServiceSlot;
import com.kltn.scsms_api_service.core.service.businessService.ServiceSlotManagementService;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Service Slot Management", description = "APIs for managing service slots")
public class ServiceSlotManagementController {
    
    private final ServiceSlotManagementService serviceSlotManagementService;
    
    @GetMapping("/branches/{branchId}/slots")
    @Operation(summary = "Get all slots by branch", description = "Retrieve all service slots for a specific branch")
    @SwaggerOperation(summary = "Get all slots by branch")
    public ResponseEntity<ApiResponse<Object>> getAllSlotsByBranch(
            @Parameter(description = "Branch ID") @PathVariable UUID branchId,
            @Parameter(description = "Filter parameters") ServiceSlotFilterParam filterParam) {
        log.info("Getting all slots for branch: {}", branchId);
        
        if (filterParam != null && filterParam.getPage() >= 0 && filterParam.getSize() > 0) {
            // Return paginated results
            filterParam.setBranchId(branchId);
            Page<ServiceSlotInfoDto> slotPage = serviceSlotManagementService.getAllSlotsByBranch(filterParam);
            return ResponseBuilder.success(slotPage);
        } else {
            // Return all results
            List<ServiceSlotInfoDto> slots = serviceSlotManagementService.getAllSlotsByBranch(branchId);
            return ResponseBuilder.success(slots);
        }
    }
    
    @GetMapping("/slots/{slotId}")
    @Operation(summary = "Get slot by ID", description = "Retrieve a specific service slot by its ID")
    @SwaggerOperation(summary = "Get slot by ID")
    public ResponseEntity<ApiResponse<ServiceSlotInfoDto>> getSlotById(
            @Parameter(description = "Slot ID") @PathVariable UUID slotId) {
        log.info("Getting slot by ID: {}", slotId);
        ServiceSlotInfoDto slot = serviceSlotManagementService.getSlotById(slotId);
        return ResponseBuilder.success(slot);
    }
    
    @GetMapping("/branches/{branchId}/slots/date/{slotDate}")
    @Operation(summary = "Get slots by branch and date", description = "Retrieve all service slots for a specific branch and date")
    @SwaggerOperation(summary = "Get slots by branch and date")
    public ResponseEntity<ApiResponse<List<ServiceSlotInfoDto>>> getSlotsByBranchAndDate(
            @Parameter(description = "Branch ID") @PathVariable UUID branchId,
            @Parameter(description = "Slot date") @PathVariable LocalDate slotDate) {
        log.info("Getting slots for branch: {} on date: {}", branchId, slotDate);
        List<ServiceSlotInfoDto> slots = serviceSlotManagementService.getSlotsByBranchAndDate(branchId, slotDate);
        return ResponseBuilder.success(slots);
    }
    
    @GetMapping("/branches/{branchId}/slots/date/{slotDate}/available")
    @Operation(summary = "Get available slots by branch and date", description = "Retrieve all available service slots for a specific branch and date")
    @SwaggerOperation(summary = "Get available slots by branch and date")
    public ResponseEntity<ApiResponse<List<ServiceSlotInfoDto>>> getAvailableSlotsByBranchAndDate(
            @Parameter(description = "Branch ID") @PathVariable UUID branchId,
            @Parameter(description = "Slot date") @PathVariable LocalDate slotDate) {
        log.info("Getting available slots for branch: {} on date: {}", branchId, slotDate);
        List<ServiceSlotInfoDto> slots = serviceSlotManagementService.getAvailableSlotsByBranchAndDate(branchId, slotDate);
        return ResponseBuilder.success(slots);
    }
    
    @GetMapping("/branches/{branchId}/slots/date/{slotDate}/category/{category}")
    @Operation(summary = "Get slots by category", description = "Retrieve service slots by category for a specific branch and date")
    @SwaggerOperation(summary = "Get slots by category")
    public ResponseEntity<ApiResponse<List<ServiceSlotInfoDto>>> getSlotsByCategory(
            @Parameter(description = "Branch ID") @PathVariable UUID branchId,
            @Parameter(description = "Slot date") @PathVariable LocalDate slotDate,
            @Parameter(description = "Slot category") @PathVariable ServiceSlot.SlotCategory category) {
        log.info("Getting slots for branch: {} on date: {} with category: {}", branchId, slotDate, category);
        List<ServiceSlotInfoDto> slots = serviceSlotManagementService.getSlotsByCategory(branchId, slotDate, category);
        return ResponseBuilder.success(slots);
    }
    
    @GetMapping("/branches/{branchId}/slots/date/{slotDate}/category/{category}/available")
    @Operation(summary = "Get available slots by category", description = "Retrieve available service slots by category for a specific branch and date")
    @SwaggerOperation(summary = "Get available slots by category")
    public ResponseEntity<ApiResponse<List<ServiceSlotInfoDto>>> getAvailableSlotsByCategory(
            @Parameter(description = "Branch ID") @PathVariable UUID branchId,
            @Parameter(description = "Slot date") @PathVariable LocalDate slotDate,
            @Parameter(description = "Slot category") @PathVariable ServiceSlot.SlotCategory category) {
        log.info("Getting available slots for branch: {} on date: {} with category: {}", branchId, slotDate, category);
        List<ServiceSlotInfoDto> slots = serviceSlotManagementService.getAvailableSlotsByCategory(branchId, slotDate, category);
        return ResponseBuilder.success(slots);
    }
    
    @GetMapping("/branches/{branchId}/slots/date/{slotDate}/vip/available")
    @Operation(summary = "Get available VIP slots", description = "Retrieve all available VIP service slots for a specific branch and date")
    @SwaggerOperation(summary = "Get available VIP slots")
    public ResponseEntity<ApiResponse<List<ServiceSlotInfoDto>>> getAvailableVipSlots(
            @Parameter(description = "Branch ID") @PathVariable UUID branchId,
            @Parameter(description = "Slot date") @PathVariable LocalDate slotDate) {
        log.info("Getting available VIP slots for branch: {} on date: {}", branchId, slotDate);
        List<ServiceSlotInfoDto> slots = serviceSlotManagementService.getAvailableVipSlots(branchId, slotDate);
        return ResponseBuilder.success(slots);
    }
    
    @GetMapping("/branches/{branchId}/slots/date/{slotDate}/time-range")
    @Operation(summary = "Get slots in time range", description = "Retrieve service slots within a specific time range")
    @SwaggerOperation(summary = "Get slots in time range")
    public ResponseEntity<ApiResponse<List<ServiceSlotInfoDto>>> getSlotsInTimeRange(
            @Parameter(description = "Branch ID") @PathVariable UUID branchId,
            @Parameter(description = "Slot date") @PathVariable LocalDate slotDate,
            @Parameter(description = "Start time") @RequestParam LocalTime startTime,
            @Parameter(description = "End time") @RequestParam LocalTime endTime) {
        log.info("Getting slots for branch: {} on date: {} in time range: {} - {}", branchId, slotDate, startTime, endTime);
        List<ServiceSlotInfoDto> slots = serviceSlotManagementService.getSlotsInTimeRange(branchId, slotDate, startTime, endTime);
        return ResponseBuilder.success(slots);
    }
    
    @GetMapping("/branches/{branchId}/slots/date-range")
    @Operation(summary = "Get slots by date range", description = "Retrieve service slots within a date range")
    @SwaggerOperation(summary = "Get slots by date range")
    public ResponseEntity<ApiResponse<List<ServiceSlotInfoDto>>> getSlotsByDateRange(
            @Parameter(description = "Branch ID") @PathVariable UUID branchId,
            @Parameter(description = "Start date") @RequestParam LocalDate startDate,
            @Parameter(description = "End date") @RequestParam LocalDate endDate) {
        log.info("Getting slots for branch: {} in date range: {} - {}", branchId, startDate, endDate);
        List<ServiceSlotInfoDto> slots = serviceSlotManagementService.getSlotsByDateRange(branchId, startDate, endDate);
        return ResponseBuilder.success(slots);
    }
    
    @GetMapping("/branches/{branchId}/slots/future/available")
    @Operation(summary = "Get future available slots", description = "Retrieve all future available service slots for a specific branch")
    @SwaggerOperation(summary = "Get future available slots")
    public ResponseEntity<ApiResponse<List<ServiceSlotInfoDto>>> getFutureAvailableSlots(
            @Parameter(description = "Branch ID") @PathVariable UUID branchId,
            @Parameter(description = "From date") @RequestParam(required = false) LocalDate fromDate) {
        if (fromDate == null) {
            fromDate = LocalDate.now();
        }
        log.info("Getting future available slots for branch: {} from date: {}", branchId, fromDate);
        List<ServiceSlotInfoDto> slots = serviceSlotManagementService.getFutureAvailableSlots(branchId, fromDate);
        return ResponseBuilder.success(slots);
    }
    
    @PostMapping("/slots")
    @Operation(summary = "Create service slot", description = "Create a new service slot")
    @SwaggerOperation(summary = "Create service slot")
    public ResponseEntity<ApiResponse<ServiceSlotInfoDto>> createSlot(
            @Parameter(description = "Service slot creation request") @Valid @RequestBody CreateServiceSlotRequest request) {
        log.info("Creating service slot for branch: {} on date: {}", request.getBranchId(), request.getSlotDate());
        ServiceSlotInfoDto slot = serviceSlotManagementService.createSlot(request);
        return ResponseBuilder.created(slot);
    }
    
    @PostMapping("/slots/bulk")
    @Operation(summary = "Create multiple service slots", description = "Create multiple service slots at once")
    @SwaggerOperation(summary = "Create multiple service slots")
    public ResponseEntity<ApiResponse<List<ServiceSlotInfoDto>>> createMultipleSlots(
            @Parameter(description = "List of service slot creation requests") @Valid @RequestBody List<CreateServiceSlotRequest> requests) {
        log.info("Creating {} service slots", requests.size());
        List<ServiceSlotInfoDto> slots = serviceSlotManagementService.createMultipleSlots(requests);
        return ResponseBuilder.created(slots);
    }
    
    @PostMapping("/slots/pattern")
    @Operation(summary = "Create slots by pattern", description = "Create service slots by pattern (e.g., for a week)")
    @SwaggerOperation(summary = "Create slots by pattern")
    public ResponseEntity<ApiResponse<List<ServiceSlotInfoDto>>> createSlotsByPattern(
            @Parameter(description = "Branch ID") @RequestParam UUID branchId,
            @Parameter(description = "Start date") @RequestParam LocalDate startDate,
            @Parameter(description = "End date") @RequestParam LocalDate endDate,
            @Parameter(description = "Start time") @RequestParam LocalTime startTime,
            @Parameter(description = "End time") @RequestParam LocalTime endTime,
            @Parameter(description = "Slot category") @RequestParam ServiceSlot.SlotCategory category,
            @Parameter(description = "Priority order") @RequestParam(required = false) Integer priorityOrder) {
        if (priorityOrder == null) {
            priorityOrder = 1;
        }
        log.info("Creating slots by pattern for branch: {} from {} to {}", branchId, startDate, endDate);
        List<ServiceSlotInfoDto> slots = serviceSlotManagementService.createSlotsByPattern(
                branchId, startDate, endDate, startTime, endTime, category, priorityOrder);
        return ResponseBuilder.created(slots);
    }
    
    @PostMapping("/slots/{slotId}/update")
    @Operation(summary = "Update service slot", description = "Update an existing service slot")
    @SwaggerOperation(summary = "Update service slot")
    public ResponseEntity<ApiResponse<ServiceSlotInfoDto>> updateSlot(
            @Parameter(description = "Slot ID") @PathVariable UUID slotId,
            @Parameter(description = "Service slot update request") @Valid @RequestBody UpdateServiceSlotRequest request) {
        log.info("Updating service slot: {}", slotId);
        ServiceSlotInfoDto slot = serviceSlotManagementService.updateSlot(slotId, request);
        return ResponseBuilder.success(slot);
    }
    
    @PostMapping("/slots/{slotId}/delete")
    @Operation(summary = "Delete service slot", description = "Delete a service slot (soft delete)")
    @SwaggerOperation(summary = "Delete service slot")
    public ResponseEntity<ApiResponse<Void>> deleteSlot(
            @Parameter(description = "Slot ID") @PathVariable UUID slotId) {
        log.info("Deleting service slot: {}", slotId);
        serviceSlotManagementService.deleteSlot(slotId);
        return ResponseBuilder.success("Service slot deleted successfully");
    }
    
    @PostMapping("/slots/{slotId}/close")
    @Operation(summary = "Close service slot", description = "Close a service slot temporarily")
    @SwaggerOperation(summary = "Close service slot")
    public ResponseEntity<ApiResponse<Void>> closeSlot(
            @Parameter(description = "Slot ID") @PathVariable UUID slotId,
            @Parameter(description = "Close reason") @RequestBody Map<String, String> request) {
        String reason = request.get("reason");
        log.info("Closing service slot: {} with reason: {}", slotId, reason);
        serviceSlotManagementService.closeSlot(slotId, reason);
        return ResponseBuilder.success("Service slot closed successfully");
    }
    
    @PostMapping("/slots/{slotId}/open")
    @Operation(summary = "Open service slot", description = "Reopen a closed service slot")
    @SwaggerOperation(summary = "Open service slot")
    public ResponseEntity<ApiResponse<Void>> openSlot(
            @Parameter(description = "Slot ID") @PathVariable UUID slotId) {
        log.info("Opening service slot: {}", slotId);
        serviceSlotManagementService.openSlot(slotId);
        return ResponseBuilder.success("Service slot opened successfully");
    }
    
    @GetMapping("/branches/{branchId}/slots/date/{slotDate}/statistics")
    @Operation(summary = "Get slot statistics", description = "Get statistics for service slots on a specific date")
    @SwaggerOperation(summary = "Get slot statistics")
    public ResponseEntity<ApiResponse<ServiceSlotManagementService.SlotStatisticsDto>> getSlotStatistics(
            @Parameter(description = "Branch ID") @PathVariable UUID branchId,
            @Parameter(description = "Slot date") @PathVariable LocalDate slotDate) {
        log.info("Getting slot statistics for branch: {} on date: {}", branchId, slotDate);
        ServiceSlotManagementService.SlotStatisticsDto statistics = serviceSlotManagementService.getSlotStatistics(branchId, slotDate);
        return ResponseBuilder.success(statistics);
    }
}
