package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.response.PaginatedResponse;
import com.kltn.scsms_api_service.core.dto.supplierManagement.SupplierInfoDto;
import com.kltn.scsms_api_service.core.dto.supplierManagement.param.SupplierFilterParam;
import com.kltn.scsms_api_service.core.dto.supplierManagement.request.CreateSupplierRequest;
import com.kltn.scsms_api_service.core.dto.supplierManagement.request.UpdateSupplierRequest;
import com.kltn.scsms_api_service.core.service.businessService.SupplierManagementService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller handling supplier management operations
 * Manages supplier creation, updates, and supplier details retrieval
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Supplier Management", description = "Supplier management endpoints")
public class SupplierManagementController {
    
    private final SupplierManagementService supplierManagementService;
    
    @GetMapping(ApiConstant.GET_ALL_SUPPLIERS_API)
    @SwaggerOperation(
        summary = "Get all suppliers",
        description = "Retrieve a paginated list of all suppliers that can be filtered by name, contact person, email, etc.")
//    @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<PaginatedResponse<SupplierInfoDto>>> getAllSuppliers(@ModelAttribute SupplierFilterParam supplierFilterParam) {
        log.info("Fetching all suppliers with filters: {}", supplierFilterParam);
        
        Page<SupplierInfoDto> suppliers = supplierManagementService.getAllSuppliers(supplierFilterParam.standardizeFilterRequest(supplierFilterParam));
        
        return ResponseBuilder.paginated("Suppliers fetched successfully", suppliers);
    }
    
    @GetMapping(ApiConstant.GET_SUPPLIER_BY_ID_API)
    @SwaggerOperation(
        summary = "Get supplier by ID",
        description = "Retrieve detailed information about a specific supplier by its unique identifier.")
//    @RequireRole(roles = {"ADMIN", "MANAGER"})
    public ResponseEntity<ApiResponse<SupplierInfoDto>> getSupplierById(@PathVariable("supplierId") String supplierId) {
        log.info("Fetching supplier with ID: {}", supplierId);
        
        SupplierInfoDto supplier = supplierManagementService.getSupplierById(UUID.fromString(supplierId));
        
        return ResponseBuilder.success("Supplier fetched successfully", supplier);
    }
    
    @PostMapping(ApiConstant.CREATE_SUPPLIER_API)
    @SwaggerOperation(
        summary = "Create a new supplier",
        description = "Add a new supplier to the system with details like name, contact person, email, etc.")
//    @RequireRole(roles = {"ADMIN", "MANAGER"})
    public ResponseEntity<ApiResponse<SupplierInfoDto>> createSupplier(@RequestBody CreateSupplierRequest request) {
        log.info("Creating new supplier with name: {}", request.getSupplierName());
        
        SupplierInfoDto createdSupplier = supplierManagementService.createSupplier(request);
        
        return ResponseBuilder.success("Supplier created successfully", createdSupplier);
    }
    
    @PostMapping(ApiConstant.UPDATE_SUPPLIER_API)
    @SwaggerOperation(
        summary = "Update an existing supplier",
        description = "Modify the details of an existing supplier by its ID.")
//    @RequireRole(roles = {"ADMIN", "MANAGER"})
    public ResponseEntity<ApiResponse<SupplierInfoDto>> updateSupplier(
        @PathVariable("supplierId") String supplierId,
        @RequestBody UpdateSupplierRequest request) {
        log.info("Updating supplier with ID: {}", supplierId);
        
        SupplierInfoDto updatedSupplier = supplierManagementService.updateSupplier(UUID.fromString(supplierId), request);
        
        return ResponseBuilder.success("Supplier updated successfully", updatedSupplier);
    }
    
    @PostMapping(ApiConstant.DELETE_SUPPLIER_API)
    @SwaggerOperation(
        summary = "Delete a supplier",
        description = "Remove a supplier from the system by its ID.")
//    @RequireRole(roles = {"ADMIN", "MANAGER"})
    public ResponseEntity<ApiResponse<Void>> deleteSupplier(@PathVariable("supplierId") String supplierId) {
        log.info("Deleting supplier with ID: {}", supplierId);
        
        supplierManagementService.deleteSupplier(UUID.fromString(supplierId));
        
        return ResponseBuilder.success("Supplier deleted successfully");
    }
}
