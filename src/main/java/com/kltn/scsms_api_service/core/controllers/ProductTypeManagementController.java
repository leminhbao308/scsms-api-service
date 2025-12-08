package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.productTypeManagement.ProductTypeInfoDto;
import com.kltn.scsms_api_service.core.dto.productTypeManagement.param.ProductTypeFilterParam;
import com.kltn.scsms_api_service.core.dto.productTypeManagement.request.CreateProductTypeRequest;
import com.kltn.scsms_api_service.core.dto.productTypeManagement.request.UpdateProductTypeRequest;
import com.kltn.scsms_api_service.core.dto.productTypeManagement.request.UpdateProductTypeStatusRequest;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.response.PaginatedResponse;
import com.kltn.scsms_api_service.core.service.businessService.ProductTypeManagementService;
import com.kltn.scsms_api_service.core.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Controller handling product type management operations
 * Manages product type CRUD operations and status updates
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Type Management", description = "Product type management endpoints for CRUD operations")
public class ProductTypeManagementController {

    private final ProductTypeManagementService productTypeManagementService;

    /**
     * Get all product types with pagination and filtering
     */
    @GetMapping(ApiConstant.GET_ALL_PRODUCT_TYPES_API)
    @SwaggerOperation(summary = "Get all product types", description = "Retrieve a paginated list of all product types with filtering options")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<PaginatedResponse<ProductTypeInfoDto>>> getAllProductTypes(
            @ModelAttribute ProductTypeFilterParam productTypeFilterParam) {
        log.info("Fetching all product types with filters: {}", productTypeFilterParam);

        Page<ProductTypeInfoDto> productTypes = productTypeManagementService.getAllProductTypes(
                ProductTypeFilterParam.standardize(productTypeFilterParam));

        return ResponseBuilder.paginated("Product types fetched successfully", productTypes);
    }

    /**
     * Get product type by ID
     */
    @GetMapping(ApiConstant.GET_PRODUCT_TYPE_BY_ID_API)
    @SwaggerOperation(summary = "Get product type by ID", description = "Retrieve a specific product type by its ID")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<ProductTypeInfoDto>> getProductTypeById(
            @PathVariable("productTypeId") String productTypeId) {
        log.info("Fetching product type by ID: {}", productTypeId);

        ProductTypeInfoDto productType = productTypeManagementService.getProductTypeById(
                UUID.fromString(productTypeId));

        return ResponseBuilder.success("Product type fetched successfully", productType);
    }

    /**
     * Get product type by code
     */
    @GetMapping(ApiConstant.GET_PRODUCT_TYPE_BY_CODE_API)
    @SwaggerOperation(summary = "Get product type by code", description = "Retrieve a specific product type by its code")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<ProductTypeInfoDto>> getProductTypeByCode(
            @PathVariable("productTypeCode") String productTypeCode) {
        log.info("Fetching product type by code: {}", productTypeCode);

        ProductTypeInfoDto productType = productTypeManagementService.getProductTypeByCode(productTypeCode);

        return ResponseBuilder.success("Product type fetched successfully", productType);
    }

    /**
     * Get product types by category
     */
    @GetMapping(ApiConstant.GET_PRODUCT_TYPES_BY_CATEGORY_API)
    @SwaggerOperation(summary = "Get product types by category", description = "Retrieve all product types belonging to a specific category")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<List<ProductTypeInfoDto>>> getProductTypesByCategory(
            @PathVariable("categoryId") String categoryId) {
        log.info("Fetching product types by category ID: {}", categoryId);

        List<ProductTypeInfoDto> productTypes = productTypeManagementService.getProductTypesByCategory(
                UUID.fromString(categoryId));

        return ResponseBuilder.success("Product types fetched successfully", productTypes);
    }

    /**
     * Get active product types
     */
    @GetMapping(ApiConstant.GET_ACTIVE_PRODUCT_TYPES_API)
    @SwaggerOperation(summary = "Get active product types", description = "Retrieve all active product types")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<List<ProductTypeInfoDto>>> getActiveProductTypes() {
        log.info("Fetching active product types");

        List<ProductTypeInfoDto> productTypes = productTypeManagementService.getActiveProductTypes();

        return ResponseBuilder.success("Active product types fetched successfully", productTypes);
    }

    /**
     * Create a new product type
     */
    @PostMapping(ApiConstant.CREATE_PRODUCT_TYPE_API)
    @SwaggerOperation(summary = "Create a new product type", description = "Create a new product type with category association")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<ProductTypeInfoDto>> createProductType(
            @Valid @RequestBody CreateProductTypeRequest createRequest) {
        log.info("Creating new product type with name: {} and code: {}",
                createRequest.getProductTypeName(), createRequest.getProductTypeCode());

        ProductTypeInfoDto createdProductType = productTypeManagementService.createProductType(createRequest);

        return ResponseBuilder.created("Product type created successfully", createdProductType);
    }

    /**
     * Update an existing product type
     */
    @PostMapping(ApiConstant.UPDATE_PRODUCT_TYPE_API)
    @SwaggerOperation(summary = "Update an existing product type", description = "Update product type details including name, code, description, and category")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<ProductTypeInfoDto>> updateProductType(
            @PathVariable("productTypeId") String productTypeId,
            @Valid @RequestBody UpdateProductTypeRequest updateRequest) {
        log.info("Updating product type with ID: {}", productTypeId);

        ProductTypeInfoDto updatedProductType = productTypeManagementService.updateProductType(
                UUID.fromString(productTypeId), updateRequest);

        return ResponseBuilder.success("Product type updated successfully", updatedProductType);
    }

    /**
     * Delete a product type (soft delete)
     */
    @PostMapping(ApiConstant.DELETE_PRODUCT_TYPE_API)
    @SwaggerOperation(summary = "Delete a product type", description = "Soft delete a product type by marking it as deleted")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<Void>> deleteProductType(
            @PathVariable("productTypeId") String productTypeId) {
        log.info("Soft deleting product type with ID: {}", productTypeId);

        productTypeManagementService.deleteProductType(UUID.fromString(productTypeId));

        return ResponseBuilder.success("Product type deleted successfully");
    }

    /**
     * Update product type status (isActive) only
     */
    @PostMapping(ApiConstant.UPDATE_PRODUCT_TYPE_STATUS_API)
    @SwaggerOperation(summary = "Update product type status", description = "Update only the active status (isActive) of a product type")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<ProductTypeInfoDto>> updateProductTypeStatus(
            @PathVariable("productTypeId") String productTypeId,
            @Valid @RequestBody UpdateProductTypeStatusRequest statusRequest) {
        log.info("Updating product type status for ID: {} to {}", productTypeId, statusRequest.getIsActive());

        ProductTypeInfoDto updatedProductType = productTypeManagementService.updateProductTypeStatus(
                UUID.fromString(productTypeId), statusRequest);

        return ResponseBuilder.success("Product type status updated successfully", updatedProductType);
    }

    /**
     * Validate product type code
     */
    @GetMapping(ApiConstant.VALIDATE_PRODUCT_TYPE_CODE_API)
    @SwaggerOperation(summary = "Validate product type code", description = "Check if a product type code is available for use")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<Boolean>> validateProductTypeCode(
            @RequestParam("code") String productTypeCode) {
        log.info("Validating product type code: {}", productTypeCode);

        boolean isValid = productTypeManagementService.validateProductTypeCode(productTypeCode);

        return ResponseBuilder.success("Product type code validation completed", isValid);
    }

    /**
     * Get product type statistics
     */
    @GetMapping(ApiConstant.GET_PRODUCT_TYPE_STATISTICS_API)
    @SwaggerOperation(summary = "Get product type statistics", description = "Retrieve statistics about product types")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<ProductTypeManagementService.ProductTypeStatsDto>> getProductTypeStatistics() {
        log.info("Fetching product type statistics");

        ProductTypeManagementService.ProductTypeStatsDto statistics = 
            productTypeManagementService.getProductTypeStatistics();

        return ResponseBuilder.success("Product type statistics fetched successfully", statistics);
    }
}
