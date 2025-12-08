package com.kltn.scsms_api_service.core.controllers;

import com.kltn.scsms_api_service.annotations.SwaggerOperation;
import com.kltn.scsms_api_service.constants.ApiConstant;
import com.kltn.scsms_api_service.core.dto.productAttributeManagement.ProductAttributeInfoDto;
import com.kltn.scsms_api_service.core.dto.productAttributeManagement.param.ProductAttributeFilterParam;
import com.kltn.scsms_api_service.core.dto.productAttributeManagement.request.CreateProductAttributeRequest;
import com.kltn.scsms_api_service.core.dto.productAttributeManagement.request.UpdateProductAttributeRequest;
import com.kltn.scsms_api_service.core.dto.productAttributeManagement.request.UpdateProductAttributeStatusRequest;
import com.kltn.scsms_api_service.core.dto.response.ApiResponse;
import com.kltn.scsms_api_service.core.dto.response.PaginatedResponse;
import com.kltn.scsms_api_service.core.service.businessService.ProductAttributeManagementService;
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
 * Controller handling product attribute management operations
 * Manages product attributes for defining product characteristics
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Attribute Management", description = "Product attribute management endpoints for CRUD operations")
public class ProductAttributeManagementController {

    private final ProductAttributeManagementService productAttributeManagementService;

    /**
     * Get all product attributes with pagination and filtering
     */
    @GetMapping(ApiConstant.GET_ALL_PRODUCT_ATTRIBUTES_API)
    @SwaggerOperation(summary = "Get all product attributes", description = "Retrieve a paginated list of all product attributes with filtering options")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<PaginatedResponse<ProductAttributeInfoDto>>> getAllProductAttributes(
            @ModelAttribute ProductAttributeFilterParam productAttributeFilterParam) {
        log.info("Fetching all product attributes with filters: {}", productAttributeFilterParam);

        Page<ProductAttributeInfoDto> productAttributes = productAttributeManagementService.getAllProductAttributes(
                ProductAttributeFilterParam.standardize(productAttributeFilterParam));

        return ResponseBuilder.paginated("Product attributes fetched successfully", productAttributes);
    }

    /**
     * Get product attribute by ID
     */
    @GetMapping(ApiConstant.GET_PRODUCT_ATTRIBUTE_BY_ID_API)
    @SwaggerOperation(summary = "Get product attribute by ID", description = "Retrieve a specific product attribute by its ID")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<ProductAttributeInfoDto>> getProductAttributeById(
            @PathVariable("attributeId") String attributeId) {
        log.info("Fetching product attribute by ID: {}", attributeId);

        ProductAttributeInfoDto productAttribute = productAttributeManagementService.getProductAttributeById(
                UUID.fromString(attributeId));

        return ResponseBuilder.success("Product attribute fetched successfully", productAttribute);
    }

    /**
     * Get product attribute by code
     */
    @GetMapping(ApiConstant.GET_PRODUCT_ATTRIBUTE_BY_CODE_API)
    @SwaggerOperation(summary = "Get product attribute by code", description = "Retrieve a specific product attribute by its code")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<ProductAttributeInfoDto>> getProductAttributeByCode(
            @PathVariable("attributeCode") String attributeCode) {
        log.info("Fetching product attribute by code: {}", attributeCode);

        ProductAttributeInfoDto productAttribute = productAttributeManagementService.getProductAttributeByCode(attributeCode);

        return ResponseBuilder.success("Product attribute fetched successfully", productAttribute);
    }

    /**
     * Get product attributes by data type
     */
    @GetMapping(ApiConstant.GET_PRODUCT_ATTRIBUTES_BY_DATA_TYPE_API)
    @SwaggerOperation(summary = "Get product attributes by data type", description = "Retrieve all product attributes of a specific data type")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<List<ProductAttributeInfoDto>>> getProductAttributesByDataType(
            @PathVariable("dataType") String dataType) {
        log.info("Fetching product attributes by data type: {}", dataType);

        List<ProductAttributeInfoDto> productAttributes = productAttributeManagementService.getProductAttributesByDataType(dataType);

        return ResponseBuilder.success("Product attributes fetched successfully", productAttributes);
    }

    /**
     * Get required product attributes
     */
    @GetMapping(ApiConstant.GET_REQUIRED_PRODUCT_ATTRIBUTES_API)
    @SwaggerOperation(summary = "Get required product attributes", description = "Retrieve all required product attributes")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<List<ProductAttributeInfoDto>>> getRequiredProductAttributes() {
        log.info("Fetching required product attributes");

        List<ProductAttributeInfoDto> productAttributes = productAttributeManagementService.getRequiredProductAttributes();

        return ResponseBuilder.success("Required product attributes fetched successfully", productAttributes);
    }

    /**
     * Get active product attributes
     */
    @GetMapping(ApiConstant.GET_ACTIVE_PRODUCT_ATTRIBUTES_API)
    @SwaggerOperation(summary = "Get active product attributes", description = "Retrieve all active product attributes")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<List<ProductAttributeInfoDto>>> getActiveProductAttributes() {
        log.info("Fetching active product attributes");

        List<ProductAttributeInfoDto> productAttributes = productAttributeManagementService.getActiveProductAttributes();

        return ResponseBuilder.success("Active product attributes fetched successfully", productAttributes);
    }

    /**
     * Create a new product attribute
     */
    @PostMapping(ApiConstant.CREATE_PRODUCT_ATTRIBUTE_API)
    @SwaggerOperation(summary = "Create a new product attribute", description = "Create a new product attribute for defining product characteristics")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<ProductAttributeInfoDto>> createProductAttribute(
            @Valid @RequestBody CreateProductAttributeRequest createRequest) {
        log.info("Creating new product attribute with name: {} and code: {}",
                createRequest.getAttributeName(), createRequest.getAttributeCode());

        ProductAttributeInfoDto createdProductAttribute = productAttributeManagementService.createProductAttribute(createRequest);

        return ResponseBuilder.created("Product attribute created successfully", createdProductAttribute);
    }

    /**
     * Update an existing product attribute
     */
    @PostMapping(ApiConstant.UPDATE_PRODUCT_ATTRIBUTE_API)
    @SwaggerOperation(summary = "Update an existing product attribute", description = "Update product attribute details including name, code, unit, and data type")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<ProductAttributeInfoDto>> updateProductAttribute(
            @PathVariable("attributeId") String attributeId,
            @Valid @RequestBody UpdateProductAttributeRequest updateRequest) {
        log.info("Updating product attribute with ID: {}", attributeId);

        ProductAttributeInfoDto updatedProductAttribute = productAttributeManagementService.updateProductAttribute(
                UUID.fromString(attributeId), updateRequest);

        return ResponseBuilder.success("Product attribute updated successfully", updatedProductAttribute);
    }

    /**
     * Delete a product attribute (soft delete)
     */
    @PostMapping(ApiConstant.DELETE_PRODUCT_ATTRIBUTE_API)
    @SwaggerOperation(summary = "Delete a product attribute", description = "Soft delete a product attribute by marking it as deleted")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<Void>> deleteProductAttribute(
            @PathVariable("attributeId") String attributeId) {
        log.info("Soft deleting product attribute with ID: {}", attributeId);

        productAttributeManagementService.deleteProductAttribute(UUID.fromString(attributeId));

        return ResponseBuilder.success("Product attribute deleted successfully");
    }

    /**
     * Update product attribute status (isActive) only
     */
    @PostMapping(ApiConstant.UPDATE_PRODUCT_ATTRIBUTE_STATUS_API)
    @SwaggerOperation(summary = "Update product attribute status", description = "Update only the active status (isActive) of a product attribute")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<ProductAttributeInfoDto>> updateProductAttributeStatus(
            @PathVariable("attributeId") String attributeId,
            @Valid @RequestBody UpdateProductAttributeStatusRequest statusRequest) {
        log.info("Updating product attribute status for ID: {} to {}", attributeId, statusRequest.getIsActive());

        ProductAttributeInfoDto updatedProductAttribute = productAttributeManagementService.updateProductAttributeStatus(
                UUID.fromString(attributeId), statusRequest);

        return ResponseBuilder.success("Product attribute status updated successfully", updatedProductAttribute);
    }

    /**
     * Validate product attribute code
     */
    @GetMapping(ApiConstant.VALIDATE_PRODUCT_ATTRIBUTE_CODE_API)
    @SwaggerOperation(summary = "Validate product attribute code", description = "Check if a product attribute code is available for use")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<Boolean>> validateProductAttributeCode(
            @RequestParam("code") String attributeCode) {
        log.info("Validating product attribute code: {}", attributeCode);

        boolean isValid = productAttributeManagementService.validateAttributeCode(attributeCode);

        return ResponseBuilder.success("Product attribute code validation completed", isValid);
    }

    /**
     * Get product attribute statistics
     */
    @GetMapping(ApiConstant.GET_PRODUCT_ATTRIBUTE_STATISTICS_API)
    @SwaggerOperation(summary = "Get product attribute statistics", description = "Retrieve statistics about product attributes")
    // @RequireRole(roles = {"ADMIN", "MANAGER", "INV_MGR"})
    public ResponseEntity<ApiResponse<ProductAttributeManagementService.ProductAttributeStatsDto>> getProductAttributeStatistics() {
        log.info("Fetching product attribute statistics");

        ProductAttributeManagementService.ProductAttributeStatsDto statistics = 
            productAttributeManagementService.getProductAttributeStatistics();

        return ResponseBuilder.success("Product attribute statistics fetched successfully", statistics);
    }
}
