package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.core.dto.priceManagement.param.PriceListFilterParam;
import com.kltn.scsms_api_service.core.dto.priceManagement.request.PriceListHeaderRequest;
import com.kltn.scsms_api_service.core.dto.priceManagement.response.PriceListHeaderResponse;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.response.PaginatedResponse;
import com.kltn.scsms_api_service.core.entity.enumAttribute.CustomerRank;
import com.kltn.scsms_api_service.core.service.PriceListService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/price-lists")
public class PriceListController {
    
    private final PriceListService priceListService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SwaggerOperation(summary = "Create a new price list", description = "Create a new price list with details for products, services, or packages")
    public ResponseEntity<ApiResponse<PriceListHeaderResponse>> createPriceList(@RequestBody PriceListHeaderRequest request) {
        return ResponseBuilder.success(priceListService.createPriceList(request));
    }
    
    @GetMapping("/{id}")
    @SwaggerOperation(summary = "Get price list by ID", description = "Retrieve price list details by ID")
    public ResponseEntity<ApiResponse<PriceListHeaderResponse>> getPriceListById(@PathVariable("id") UUID priceListId) {
        return ResponseBuilder.success(priceListService.getPriceListById(priceListId));
    }
    
    @GetMapping("/code/{code}")
    @SwaggerOperation(summary = "Get price list by code", description = "Retrieve price list details by price list code")
    public ResponseEntity<ApiResponse<PriceListHeaderResponse>> getPriceListByCode(@PathVariable("code") String priceListCode) {
        return ResponseBuilder.success(priceListService.getPriceListByCode(priceListCode));
    }
    
    @PutMapping("/{id}")
    @SwaggerOperation(summary = "Update price list", description = "Update an existing price list")
    public ResponseEntity<ApiResponse<PriceListHeaderResponse>> updatePriceList(
        @PathVariable("id") UUID priceListId,
        @RequestBody PriceListHeaderRequest request) {
        return ResponseBuilder.success(priceListService.updatePriceList(priceListId, request));
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SwaggerOperation(summary = "Delete price list", description = "Delete a price list (only allowed for DRAFT or INACTIVE status)")
    public ResponseEntity<ApiResponse<Void>> deletePriceList(@PathVariable("id") UUID priceListId) {
        priceListService.deletePriceList(priceListId);
        
        return ResponseBuilder.success("Price list deleted successfully");
    }
    
    @GetMapping
    @SwaggerOperation(summary = "Search price lists", description = "Search and filter price lists")
    public ResponseEntity<ApiResponse<PaginatedResponse<PriceListHeaderResponse>>> searchPriceLists(PriceListFilterParam filterParam) {
        return ResponseBuilder.paginated(priceListService.searchPriceLists(filterParam));
    }
    
    @PatchMapping("/{id}/status")
    @SwaggerOperation(summary = "Update price list status", description = "Update the status of a price list (e.g., activate, deactivate)")
    public ResponseEntity<ApiResponse<PriceListHeaderResponse>> updatePriceListStatus(
        @PathVariable("id") UUID priceListId,
        @RequestParam String status) {
        return ResponseBuilder.success(priceListService.updatePriceListStatus(priceListId, status));
    }
    
    @GetMapping("/branch/{branchId}/active")
    @SwaggerOperation(summary = "Get active price lists for branch", description = "Retrieve all active price lists applicable to a specific branch")
    public ResponseEntity<ApiResponse<List<PriceListHeaderResponse>>> getActivePriceListsByBranch(
        @PathVariable("branchId") UUID branchId) {
        return ResponseBuilder.success(priceListService.getActivePriceListsByBranch(branchId));
    }
    
    @GetMapping("/branch/{branchId}/customer-rank/{rank}")
    @SwaggerOperation(summary = "Get active price lists for branch and customer rank", description = "Retrieve all active price lists applicable to a specific branch and customer rank")
    public ResponseEntity<ApiResponse<List<PriceListHeaderResponse>>> getActivePriceListsByBranchAndCustomerRank(
        @PathVariable("branchId") UUID branchId,
        @PathVariable("rank") CustomerRank rank) {
        return ResponseBuilder.success(priceListService.getActivePriceListsByBranchAndCustomerRank(branchId, rank));
    }
}
